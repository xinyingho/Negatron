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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.loader.SoftwareListLoader;
import net.babelsoft.negatron.io.loader.ThreadedCacheLoader;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.model.statistics.SoftwareStatistics;

/**
 *
 * @author capan
 */
public class SoftwareListCache extends Cache<SoftwareListCache.Data, SoftwareListCache.Version> implements ThreadedCacheLoader<Callable<Void>> {

    protected static class Version extends HashMap<String, Instant> {
        static final long serialVersionUID = 1L;
    }
    
    protected static class Data extends HashMap<String, SoftwareList> {
        static final long serialVersionUID = 2L;
        
        private SoftwareStatistics statistics = new SoftwareStatistics();
        
        // TODO: override other remove methods?
        @Override
        public SoftwareList remove(Object key) {
            statistics.remove(key.toString());
            return super.remove(key);
        }
    }
    
    public static String convertPathToName(Path path) {
        // convert path to software list name (fact as of MAME 0.169: filename is the softlist name even if /softwarelist/@name within the xml file differs)
        return path.getFileName().toString().replaceFirst("\\.xml$", "");
    }
    
    private Data data;
    
    private int processingfileCount;
    private AtomicInteger processedfileCount;
    
    public SoftwareListCache() throws ClassNotFoundException, IOException {
        super("softlist");
    }
    
    public Map<String, SoftwareList> get() {
        return data;
    }
    
    public SoftwareStatistics getStatistics() {
        return data.statistics;
    }
    
    @Override
    protected Version loadVersion() throws ClassNotFoundException, IOException {
        Version _version = null;
        try {
            _version = super.loadVersion();
        } catch (Exception ex) {
            Logger.getLogger(SoftwareListCache.class.getName()).log(Level.WARNING, null, ex);
        }
        if (_version == null)
            _version = new Version();
        return _version;
    }
    
    @Override
    public Data load() throws ClassNotFoundException, IOException {
        try {
            data = super.load();
        } catch (Exception ex) {
            Logger.getLogger(SoftwareListCache.class.getName()).log(Level.WARNING, null, ex);
        }
        if (data == null) {
            data = new Data();
            version.clear();
        }
        return data;
    }

    @Override
    public List<Callable<Void>> threadedLoad() throws ClassNotFoundException, IOException {
        List<Callable<Void>> loaders = new ArrayList<>();
        load();
        
        // check for new or updated dat files
        for (String hashString : Configuration.Manager.getFolderPaths(Property.HASH)) {
            Path hashPath = Paths.get(hashString);
            if (Files.exists(hashPath)) Files.find(
                hashPath, 1, (path, attr) -> path.toString().endsWith(".xml")
            ).forEach(
                path -> { try {
                    String key = path.toString();
                    if (Files.exists(path)) {
                        Instant value = Files.getLastModifiedTime(path).toInstant();
                        if (!version.containsKey(key) || !version.get(key).equals(value)) { 
                            loaders.add(new SoftwareListLoader(this, path));
                            version.put(key, value);
                            ++processingfileCount;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SoftwareListCache.class.getName()).log(Level.SEVERE, null, ex);
                }}
            );
        }
        
        // check for deleted dat files
        List<String> removal = new ArrayList<>();
        for (String key : version.keySet()) {
            if (!Files.exists( Paths.get(key) ))
                removal.add(key);
        }
        removal.forEach(key -> {
            // dat file deleted, so remove its content from cache
            version.remove(key);
            key = convertPathToName(Paths.get(key));
            data.remove(key);
        });
        
        // update state accordingly to previous checks
        if (processingfileCount > 0) {
            clearVersion(); // flag cache as temporarily invalid as it's about to be updated
            processedfileCount = new AtomicInteger();
        } else if (removal.size() > 0)
            save();
        
        return loaders;
    }
    
    public void save(SoftwareList softwareList) throws IOException {
        data.put(softwareList.getName(), softwareList);
        
        if (processedfileCount.incrementAndGet() == processingfileCount) try {
            // cache has been entirely processed and is valid for use throughout sessions
            save();
        } catch (Exception ex) {
            Logger.getLogger(InformationCache.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    private void save() throws IOException {
        save(data);
        saveVersion();
    }
}
