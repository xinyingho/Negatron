/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.babelsoft.negatron.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author capan
 */
public final class PathUtil {
    
    private PathUtil() { }
    
    public enum PathType {
        FILE,
        FOLDER
    }
    
    public static Path retrieveFromJavaLibraryPaths(PathType pathType, String... pathComponents) {
        // Retrieve all the potential root folders
        final List<String> rootFolders = new ArrayList<>();
        rootFolders.add(""); // default path to the current working folder
        String exePath = System.getProperty("jpackage.app-path");
        if (Strings.isValid(exePath)) {
            // required for the Linux packaged versions
            Path appPath = Paths.get(exePath).getParent().resolveSibling("lib/app");
            rootFolders.add(appPath.toString());
        }
        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath != null)
            rootFolders.addAll( Arrays.asList(libraryPath.split(File.pathSeparator)) );

        // Search for the first valid path over all those root folders
        final Optional<Path> opath = rootFolders.stream().map(
            path -> Paths.get(path, pathComponents)
        ).filter(
            path -> Files.exists(path) && (
                pathType == PathType.FILE && Files.isRegularFile(path) ||
                pathType == PathType.FOLDER && Files.isDirectory(path)
            )
        ).findFirst();
        
        return opath.orElse(null);
    }
}
