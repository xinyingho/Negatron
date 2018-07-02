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
package net.babelsoft.negatron.io.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.scene.image.Image;
import net.babelsoft.negatron.io.cache.IconCache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.extras.Icons;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.util.IterableEnumeration;
import net.babelsoft.negatron.util.function.Delegate;

/**
 *
 * @author capan
 */
public class IconLoader implements InitialisedCallable<Void> {
    
    private final static String ZIP_EXT = ".zip";
    
    private final IconCache cache;
    private Map<String, Machine> machines;
    
    public IconLoader(IconCache cache) {
        this.cache = cache;
    }
    
    private void updateUI(String name) {
        Machine machine = machines.get(name);
        if (machine != null)
            machine.setIcon( cache.get(name) );
    }

    @Override
    public void initialise(Map<String, Machine> machines, Map<String, SoftwareList> softwareLists) {
        this.machines = machines;
    }

    @Override
    public Void call() throws Exception {
        try {
            cache.load();
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(IconLoader.class.getName()).log(
                Level.SEVERE, "Couldn't load icon cache, format may have changed, still re-building cache from source files", ex
            );
        }
        
        // refresh UI with cache content
        cache.getKeys().forEach(
            name -> updateUI(name)
        );
        
        boolean isModified = false;
        
        // refresh cache if needed
        final List<String> batch = new ArrayList<>();
        final int cutoffSize = 1000;
        Delegate batchUpdateUI = () -> {
            batch.forEach(id -> updateUI(id));
            batch.clear();
        };
        
        for (String folder : Configuration.Manager.getFolderPaths(Property.ICON)) {
            Path folderPath = Paths.get(folder);
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) try (
                Stream<Path> iconPaths = Files.list(folderPath)
            ) {
                for (Path iconPath : iconPaths.toArray(Path[]::new)) {
                    String key = cache.pathToKey(iconPath);
                    Instant version = cache.getVersion(key);
                    Instant timestamp = Files.getLastModifiedTime(iconPath).toInstant();

                    if (version == null || !version.equals(timestamp)) {
                        Image image = Icons.newImage(iconPath, IconCache.WIDTH);
                        cache.putVersion(key, timestamp);
                        cache.put(key, image);
                        // Java 8u66 isn't reliable enough to bear heavy continuous GUI updates through grid bindings without graphical update freezes
                        // so disable below realtime update and do it by batches
                        //updateUI(key);
                        batch.add(key);
                        if (batch.size() > cutoffSize)
                            batchUpdateUI.fire();

                        if (!isModified)
                            isModified = true;
                    }
                    
                    if (Thread.interrupted())
                        return null;
                }
            }
            
            String zip = folder;
            if (!zip.endsWith(ZIP_EXT))
                zip += ZIP_EXT;
            
            Path zipPath = Paths.get(zip);
            if (Files.exists(zipPath) && !Files.isDirectory(zipPath)) try (
                ZipFile zipFile = new ZipFile(zip)
            ) {
                for (ZipEntry zipEntry : IterableEnumeration.make(zipFile.entries())) {
                    if (!zipEntry.isDirectory()) {
                        String key = cache.pathToKey(Paths.get(zipEntry.getName()));
                        Instant version = cache.getVersion(key);
                        Instant timestamp = zipEntry.getLastModifiedTime().toInstant();

                        if (version == null || !version.equals(timestamp)) {
                            Image image = Icons.newImage(zipFile, zipEntry, IconCache.WIDTH);
                            cache.putVersion(key, timestamp);
                            cache.put(key, image);
                            // Java 8u66 isn't reliable enough to bear heavy continuous GUI updates through grid bindings without graphical update freezes
                            // so disable below realtime update and do it by batches
                            //updateUI(key);
                            batch.add(key);
                            if (batch.size() > cutoffSize)
                                batchUpdateUI.fire();

                            if (!isModified)
                                isModified = true;
                        }
                    }
                    
                    if (Thread.interrupted())
                        return null;
                }
            }
        }
        
        batchUpdateUI.fire();
        
        if (isModified) {
            cache.save();
            isModified = false;
        }
        
        // remove useless entries if any
        List<String> keysToRemove = new ArrayList<>();
        Map<String, ZipFile> zipFiles = new HashMap<>();
        try {
            // 1- cache zip files
            for (String zip : Configuration.Manager.getFolderPaths(Property.ICON)) {
                if (!zip.endsWith(ZIP_EXT))
                    zip += ZIP_EXT;

                Path zipPath = Paths.get(zip);
                if (Files.exists(zipPath) && !Files.isDirectory(zipPath)) try {
                    ZipFile zipFile = new ZipFile(zip);
                    zipFiles.put(zip, zipFile);
                } catch (Exception ex) { }
            }
            // 2- loop over icon cache to detect now useless entries
            Set<String> keys = cache.getKeys();
            for (String key : keys) {
                boolean exists = false;

                for (String folder : Configuration.Manager.getFolderPaths(Property.ICON)) {
                    Path path = Paths.get(folder, key + Icons.EXTENSION);
                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        exists = true;
                        break;
                    }

                    String zip = folder;
                    if (!zip.endsWith(ZIP_EXT))
                        zip += ZIP_EXT;

                    ZipFile zipFile = zipFiles.get(zip);
                    if (zipFile != null) {
                        ZipEntry zipEntry = zipFile.getEntry(key + Icons.EXTENSION);
                        if (zipEntry != null && !zipEntry.isDirectory()) {
                            exists = true;
                            break;
                        }
                    }
                }

                if (!exists)
                    keysToRemove.add(key);
                
                if (Thread.interrupted())
                    return null;
            }
        } finally {
            // 3- release zip files
            zipFiles.values().forEach(zipFile -> {
                try { zipFile.close(); } catch (Exception ex) { }
            });
        }
        if (keysToRemove.size() > 0) {
            // 4- effectively remove useless entries
            keysToRemove.forEach(key -> {
                cache.remove(key);
                updateUI(key);
            });
            if (!isModified)
                isModified = true;
        }
        
        if (isModified)
            cache.save();
        
        return null;
    }
}
