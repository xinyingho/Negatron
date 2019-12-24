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
package net.babelsoft.negatron.io.extras;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.babelsoft.negatron.io.cache.Cache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;

/**
 *
 * @author capan
 */
public class Extras {
    private final static int BUFFER_SIZE = 2048;
    private final static String PDF_EXT = ".pdf";
    private final static String TXT_EXT = ".txt";
    private final static String ZIP_EXT = ".zip";
    
    private Extras() { };
    
    public static Path toPath(String name, Property property, String... extensions) {
        return toPath(null, name, property, extensions);
    }
    
    public static Path toPath(String system, String name, Property property, String... extensions) {
        return Arrays.stream(extensions).flatMap(
            extension -> Configuration.Manager.getFolderPaths(property).stream().filter(
                stringPath -> {
                    Path path = Paths.get(stringPath);
                    return Files.exists(path) && Files.isDirectory(path);
                }
            ).map(
                stringPath -> {
                    if (system != null)
                        return Paths.get(stringPath, system, name + extension);
                    else
                        return Paths.get(stringPath, name + extension);
                }
            ).filter(
                path -> Files.exists(path) && !Files.isDirectory(path)
            )
        ).findAny().orElse(null);
    }
    
    public static String toPrimaryPath(String name, Property property) {
        return Extras.toPrimaryPath(null, name, property);
    }
    
    public static String toPrimaryPath(String system, String name, Property property) {
        if (name == null)
            return null;
        
        if (system != null) {
            String path = Configuration.Manager.getPrimarySoftwareFolder(property);
            return Paths.get(path, system, name).toString();
        } else {
            String path = Configuration.Manager.getPrimaryMachineFolder(property);
            return Paths.get(path, name).toString();
        }
    }
    
    public static Path toPdfPath(String system, String name, Property property) {
        Path path = Extras.toPath(system, name, property, PDF_EXT);
        
        if (path == null) try (
            InputStream stream = newZipInputStream(system, name, property, PDF_EXT)
        ) {
            if (stream != null) {
                Path tmpPath = Cache.ROOT_FOLDER.resolve("tmp" + PDF_EXT);
                try (
                    FileOutputStream file = new FileOutputStream(tmpPath.toFile())
                ) {
                    int count;
                    byte buffer[] = new byte[BUFFER_SIZE];
                    while ((count = stream.read(buffer, 0, BUFFER_SIZE)) != -1)
                        file.write(buffer, 0, count);
                    file.close();
                    path = tmpPath;
                }
            }
        // if any exceptions are raised, it means that no corresponding resources are available, so silently swallow exceptions and return null
        } catch (IOException ex) { }
        
        return path;
    }
    
    private static Stream<String> toZipPath(String system, String name, String... extensions) {
        List<String> res = new ArrayList<>();
        
        Arrays.stream(extensions).forEach(
            extension -> res.add(
                (system != null ? system + "/" : "") + name + extension
            )
        );
        
        return res.stream();
    }
    
    public static InputStream newInputStream(String name, Property property, String... extensions) throws IOException {
        return newInputStream(null, name, property, extensions);
    }
    
    public static InputStream newInputStream(String system, String name, Property property, String... extensions) throws IOException {
        Path path = toPath(system, name, property, extensions);
        
        if (path != null)
            return Files.newInputStream(path);
        else
            return newZipInputStream(system, name, property, extensions);
    }
    
    public static InputStream newInputStream(ZipFile file, ZipEntry entry) {
        try (
            InputStream entryStream = file.getInputStream(entry);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            BufferedOutputStream outputStream = new BufferedOutputStream(byteStream, BUFFER_SIZE);
        ) {
            int count;
            
            byte buffer[] = new byte[BUFFER_SIZE];
            while ((count = entryStream.read(buffer, 0, BUFFER_SIZE)) != -1)
                outputStream.write(buffer, 0, count);
            outputStream.flush();
            
            return new ByteArrayInputStream(byteStream.toByteArray());
        } catch (IOException ex) {
            return null;
        }
    }
    
    private static InputStream newZipInputStream(String system, String name, Property property, String... extensions) throws IOException {
        return Configuration.Manager.getFolderPaths(property).stream().map(
            stringPath -> stringPath.endsWith(ZIP_EXT) ? stringPath : stringPath + ZIP_EXT
        ).filter(
            stringPath -> {
                Path path = Paths.get(stringPath);
                return Files.exists(path) && !Files.isDirectory(path);
            }
        ).map(
            stringPath -> { try (
                ZipFile file = new ZipFile(stringPath)
            ) {
                return toZipPath(system, name, extensions).map(
                    zipPath -> file.getEntry(zipPath)
                ).filter(
                    zipEntry -> zipEntry != null && !zipEntry.isDirectory()
                ).findFirst().map(
                    entry -> newInputStream(file, entry)
                ).orElse(null);
            } catch (IOException ex) {
                return null;
            }}
        ).filter(
            inputStream -> inputStream != null
        ).findFirst().orElse(null);
    }
    
    private static Path getDocumentPath(String wantedFilenameMask, String removingExpression, String resource) throws IOException {
        String version = null;
        try {
            // Java 8 version of the below block: version = Extras.class.getPackage().getImplementationVersion();
            // Since Java 9 and the advent of modules, information from manifest aren't loaded anymore and so a workaround is needed
            String res = Extras.class.getResource(Extras.class.getSimpleName() + ".class").toString();
            URL url = new URL(res.substring(0, res.length() - (Extras.class.getName() + ".class").length()) + JarFile.MANIFEST_NAME);
            Manifest manifest = new Manifest(url.openStream());
            version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (IOException ex) {
            // swallow errors
        }
        Path defaultDocumentPath = Paths.get(".", String.format(wantedFilenameMask, version));
        
        if (Files.notExists(defaultDocumentPath)) {
            defaultDocumentPath = Paths.get(Configuration.getRootFolder().toString(), String.format(wantedFilenameMask, version));
            if (Files.notExists(defaultDocumentPath)) {
                // remove any old versions of the wanted document
                Files.walkFileTree(Paths.get(Configuration.getRootFolder().toString()), EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getName(file.getNameCount() - 1).toString().matches(removingExpression))
                            Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
                // extract the current version of the wanted document
                Files.copy(Extras.class.getResourceAsStream(
                    "/net/babelsoft/negatron/resource/" + resource
                ), defaultDocumentPath);
            }
        }
        
        return defaultDocumentPath;
    }
    
    public static String getDefaultPdfPath() throws IOException {
        return getDocumentPath(
            "Negatron Manual v%s" + PDF_EXT,
            "Negatron Manual[\\s\\S]*\\" + PDF_EXT,
            "Negatron Manual" + PDF_EXT
        ).toString();
    }
    
    public static String getDefaultInformationContent() throws IOException {
        Path path = getDocumentPath(
            "readme-v%s" + TXT_EXT,
            "readme[\\s\\S]*\\" + TXT_EXT,
            "readme" + TXT_EXT
        );
        return Files.lines(path).collect(Collectors.joining("\n"));
    }
}
