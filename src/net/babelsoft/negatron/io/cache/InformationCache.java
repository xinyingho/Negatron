/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2018 BabelSoft S.A.S.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.babelsoft.negatron.io.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.PathCharset;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.loader.InformationData;
import net.babelsoft.negatron.io.loader.InformationLoader;
import net.babelsoft.negatron.io.loader.InitialisedCallable;
import net.babelsoft.negatron.io.loader.ThreadedCacheLoader;

/* MAME follows a 3-level hierarchy: root > machine > software.<br />
 * As data files may provide information for machine and software, they use a 2-level hierarchy modelling those 2 kinds of information side by side:
 * <ul>
 *  <li>root > machine</li>
 *  <li>machine > software</li>
 * </ul>
 * In InformationCache class, data files are called sources and this 2-level hierarchy is called: system > item.<br />
 * <br />
 * Moreover, to avoid entry duplicates, data files may also group several similar systems (resp. items) together and reference them through the same alias.<br />
 * In InformationCache class, those aliases are managed through indexes.
 * @author capan
 */
public class InformationCache extends Cache<InformationCache.Data, InformationCache.Version> implements ThreadedCacheLoader<InitialisedCallable<Void>> {
    
    protected static class Version extends HashMap<String, Instant> {
        static final long serialVersionUID = 1L;
    }
    
    protected static class SystemIndex extends HashMap<String, Map<String, List<String>>> { // source > system > system aliases
        static final long serialVersionUID = 1L;
    }
    
    protected static class ItemIndex extends HashMap<String, Map<String, Map<String, String>>> { // source > system alias > item > item alias
        static final long serialVersionUID = 1L;
    }
    
    protected static class Data extends HashMap<String, Map<String, Map<String, String>>> { // source > system alias > item alias > content
        static final long serialVersionUID = 1L;
    }

    private static final String SYSTEM_INDEX_FILE = "info.system.index"; // system = root or machine
    private static final String ITEM_INDEX_FILE = "info.item.index"; // item = machine or software
    
    private static SystemIndex systemIndex;
    private static ItemIndex itemIndex;
    private static Data data;

    private int processingFileCount;
    private AtomicInteger processedFileCount;
    
    public InformationCache() throws ClassNotFoundException, IOException {
        super("info");
    }
    
    public Map<String, String> get(String system, String item, String parentItem) {
        Map<String, String> sourceContent = new LinkedHashMap<>();
        
        BiFunction<String, String, Boolean> retrieveContent = (source, target) -> {
            BooleanSupplier handleError = () -> {
                sourceContent.put(source, null);
                return false;
            };
            
            if (data.get(source) == null)
                return handleError.getAsBoolean();
            
            List<String> systemAliases = null;
            if (systemIndex != null) {
                Map<String, List<String>> index = systemIndex.get(source);
                if (index != null)
                    systemAliases = index.get(system);
            }
            if (systemAliases == null || systemAliases.isEmpty())
                systemAliases = Collections.singletonList(system);
            
            Optional<String> optionalContent = systemAliases.stream().map(systemAlias -> {
                if (data.get(source).get(systemAlias) == null)
                    return null;
                
                String itemAlias = null;
                if (itemIndex != null) {
                    Map<String, Map<String, String>> index = itemIndex.get(source);
                    if (index != null) {
                        Map<String, String> subIndex = index.get(systemAlias);
                        if (subIndex != null)
                            itemAlias = subIndex.get(target);
                    }
                }
                if (itemAlias == null)
                    itemAlias = target;
                
                return data.get(source).get(systemAlias).get(itemAlias);
            }).filter(
                content -> content != null
            ).findAny();
            
            if (optionalContent.isPresent()) {
                sourceContent.put(source, optionalContent.get());
                return true;
            } else
                return handleError.getAsBoolean();
        };
        
        List<PathCharset> paths = Configuration.Manager.getFilePaths(Property.INFORMATION);
        paths.stream().map(
            pathCharset -> pathToKey(pathCharset.getPath())
        ).forEach(
            source -> {
                if (!retrieveContent.apply(source, item) && parentItem != null)
                    retrieveContent.apply(source, parentItem);
            }
        );
        
        return sourceContent;
    }
    
    protected String pathToKey(Path path) {
        return path.getFileName().toString();
    }
    
    @Override
    protected Version loadVersion() throws ClassNotFoundException, IOException {
        Version _version = null;
        try {
            _version = super.loadVersion();
        } catch (Exception ex) {
            Logger.getLogger(IconCache.class.getName()).log(Level.WARNING, null, ex);
        }
        if (_version == null)
            _version = new Version();
        return _version;
    }
    
    protected void loadIndex() throws ClassNotFoundException, IOException {
        try {
            Path systemIndexPath = Paths.get(ROOT_FOLDER.toString(), SYSTEM_INDEX_FILE);
            systemIndex = load(systemIndexPath);
            Path itemIndexPath = Paths.get(ROOT_FOLDER.toString(), ITEM_INDEX_FILE);
            itemIndex = load(itemIndexPath);
        } catch (Exception ex) {
            Logger.getLogger(InformationCache.class.getName()).log(Level.WARNING, null, ex);
        }
        if (systemIndex == null || itemIndex == null) {
            systemIndex = new SystemIndex();
            itemIndex = new ItemIndex();
            version.clear();
        }
    }
    
    @Override
    public Data load() throws ClassNotFoundException, IOException {
        try {
            data = super.load();
        } catch (Exception ex) {
            Logger.getLogger(InformationCache.class.getName()).log(Level.WARNING, null, ex);
        }
        if (data == null) {
            data = new Data();
            systemIndex.clear();
            itemIndex.clear();
            version.clear();
        }
        return data;
    }
    
    @Override
    public List<InitialisedCallable<Void>> threadedLoad() throws ClassNotFoundException, IOException { try {
        List<InitialisedCallable<Void>> loaders = new ArrayList<>();
        loadIndex();
        load();
        
        // check for new or updated dat files
        List<PathCharset> pathCharsets = Configuration.Manager.getFilePaths(Property.INFORMATION);
        for (PathCharset pathCharset : pathCharsets) {
            Path path = pathCharset.getPath();
            String key = pathToKey(path);
            if (Files.exists(path)) {
                Instant value = Files.getLastModifiedTime(path).toInstant();
                if (!version.containsKey(key) || !version.get(key).equals(value)) { 
                    loaders.add(new InformationLoader(this, path));
                    version.put(key, value);
                    ++processingFileCount;
                }
            }
        }
        
        // check for deleted dat files
        List<String> removal = new ArrayList<>();
        for (String key : version.keySet()) {
            boolean exists = false;
            
            for (PathCharset pathCharset : pathCharsets) {
                Path path = pathCharset.getPath();
                String pathKey = pathToKey(path);
                if (key.equals(pathKey) && Files.exists(path)) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists)
                removal.add(key);
        }
        removal.forEach(key -> {
            // dat file deleted, so remove its content from cache
            version.remove(key);
            systemIndex.remove(key);
            itemIndex.remove(key);
            data.remove(key);
        });
        
        // update state accordingly to previous checks
        if (processingFileCount > 0) {
            clearVersion(); // flag cache as temporarily invalid as it's about to be updated
            processedFileCount = new AtomicInteger();
        } else if (removal.size() > 0)
            save();
        
        return loaders;
    } catch (ClassNotFoundException | IOException ex) {
        Logger.getLogger(InformationCache.class.getName()).log(Level.SEVERE, null, ex);
        throw ex;
    }}
    
    protected void saveIndex() throws IOException {
        Path systemIndexPath = Paths.get(ROOT_FOLDER.toString(), SYSTEM_INDEX_FILE);
        save(systemIndex, systemIndexPath);
        
        Path itemIndexPath = Paths.get(ROOT_FOLDER.toString(), ITEM_INDEX_FILE);
        save(itemIndex, itemIndexPath);
    }
    
    public void save(InformationData infoData) throws IOException {
        String key = pathToKey(infoData.getPath());
        
        systemIndex.put(key, infoData.getSystemIndex());
        itemIndex.put(key, infoData.getItemIndex());
        data.put(key, infoData.getInformation());
        
        if (processedFileCount.incrementAndGet() == processingFileCount) try {
            // cache has been entirely processed and is valid for use throughout sessions
            save();
        } catch (Exception ex) {
            Logger.getLogger(InformationCache.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    private void save() throws IOException {
        save(data);
        saveIndex();
        saveVersion();
    }
}
