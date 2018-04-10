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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author capan
 */
public class Cache<T, S> {
    
    private static final String VERSION_EXTENSION = ".version";
    protected static final String CACHE_EXTENSION = ".cache";
    public static final Path ROOT_FOLDER = Paths.get("Cache");
    
    public static void initialise() {
        if (Files.notExists(ROOT_FOLDER)) try {
            Files.createDirectory(ROOT_FOLDER);
        } catch (IOException ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, "Couldn't create cache root folder", ex);
        }
    }
    
    public static void clearAll() throws IOException {
        Cache cache = new Cache();
        cache.clear(Cache.ROOT_FOLDER);
    }

    protected final Path cachePath;
    protected final Path versionPath;
    protected S version;
    
    private Cache() {
        cachePath = null;
        versionPath = null;
    }
    
    protected Cache(String path) throws ClassNotFoundException, IOException {
        this.cachePath = Paths.get(ROOT_FOLDER.toString(), path + CACHE_EXTENSION);
        this.versionPath = Paths.get(ROOT_FOLDER.toString(), path + VERSION_EXTENSION);
        version = loadVersion();
    }
    
    public boolean exists() {
        return Files.exists(cachePath);
    }
    
    public final S getVersion() {
        return version;
    }
    
    protected void clearVersion() throws IOException {
        Files.deleteIfExists(versionPath);
    }
    
    protected void clear(Path path) throws IOException {
        if (Files.exists(path))
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.endsWith("ui.cache"))
                        Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        if (!dir.equals(path))
                            Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
    }
    
    protected S loadVersion() throws ClassNotFoundException, IOException {
        return load(versionPath);
    }
    
    public T load() throws ClassNotFoundException, IOException {
        return load(cachePath);
    }
    
    @SuppressWarnings("unchecked")
    protected <O> O load(Path path) throws ClassNotFoundException, IOException {
        try {
            if (Files.exists(path)) try (
                InputStream file = Files.newInputStream(path);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);
            ) {
                return (O) input.readObject();
            } else
                return null;
        } catch (Exception ex) {
            try { clear(path); } catch (Exception e) {} // delete file so that it doesn't trigger the same exception next time
            throw ex;
        }
    }
    
    protected void saveVersion() throws IOException {
        save(version, versionPath);
    }
    
    public void save(T content) throws IOException {
        save(content, cachePath);
    }
    
    protected void save(Object content, Path path) throws IOException {
        try (
            OutputStream file = Files.newOutputStream(path);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
        ) {
            output.writeObject(content);
        }
    }
}
