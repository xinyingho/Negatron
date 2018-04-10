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
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 *
 * @author capan
 */
public class IconCache extends Cache<IconCache.Data, IconCache.Version> {
    
    protected static class Version extends HashMap<String, Instant> {
        static final long serialVersionUID = 1L;
    }
    
    protected static class Data extends HashMap<String, byte[]> {
        static final long serialVersionUID = 1L;
    }
    
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static final int COLOURDEPTH = 4;
    
    private final Map<String, Image> data;

    protected IconCache() throws ClassNotFoundException, IOException {
        super("icon");
        data = new HashMap<>();
    }
    
    public String pathToKey(Path path) {
        String key = path.getFileName().toString();
        return key.substring(0, key.length() - 4);
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
    
    @Override
    public Data load() throws ClassNotFoundException, IOException {
        Data cache = null;
        try {
            cache = super.load();
        } catch (Exception ex) {
            Logger.getLogger(IconCache.class.getName()).log(Level.WARNING, null, ex);
        }
        
        if (cache != null)
            cache.entrySet().stream().forEach(entry -> {
                WritableImage img = new WritableImage(WIDTH, HEIGHT);
                img.getPixelWriter().setPixels(
                    0, 0, WIDTH, HEIGHT, PixelFormat.getByteBgraInstance(), entry.getValue(), 0, WIDTH * COLOURDEPTH
                );

                data.put(entry.getKey(), img);
            });
        else
            version.clear();
        
        return cache;
    }

    public Set<String> getKeys() {
        return data.keySet();
    }

    public Instant getVersion(String name) {
        return version.get(name);
    }

    public Image get(String name) {
        return data.get(name);
    }

    public void putVersion(String key, Instant timestamp) {
        version.put(key, timestamp);
    }

    public void put(String key, Image image) {
        data.put(key, image);
    }

    public void remove(String key) {
        version.remove(key);
        data.remove(key);
    }
    
    public void save() throws IOException {
        Data cache = new Data();
        data.entrySet().stream().forEach(entry -> {
            Image img = entry.getValue();
            
            PixelReader pixelReader = img.getPixelReader();
            byte[] buffer = new byte[WIDTH * HEIGHT * COLOURDEPTH];
            pixelReader.getPixels(
                0, 0, WIDTH, HEIGHT, PixelFormat.getByteBgraInstance(), buffer, 0, WIDTH * COLOURDEPTH
            );
            
            cache.put(entry.getKey(), buffer);
        });
        
        super.save(cache);
        saveVersion();
    }
}
