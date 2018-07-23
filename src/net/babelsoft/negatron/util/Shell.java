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
package net.babelsoft.negatron.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author capan
 */
public class Shell {
    
    private Shell() { }
    
    static {
        String os = System.getProperty("os.name").toLowerCase();
        win = os.contains("win");
        linux = os.contains("linux");
        macos = os.contains("mac") || os.contains("darwin");
    }
    
    private static boolean win;
    private static boolean linux;
    private static boolean macos;
    
    public static boolean isWindows() {
        return win;
    }
    
    public static boolean isLinux() {
        return linux;
    }
    
    public static boolean isMacOs() {
        return macos;
    }
    
    public static boolean isPosix() {
        return !win;
    }
    
    public static String expandPath(String path) {
        /*ProcessBuilder pb = new ProcessBuilder("bash", "-c", "ls -d " + path);
        return singleCommand(pb);*/
        return path.replaceAll("^\\$HOME/", System.getProperty("user.home") + "/");
    }
    
    public static String maskPath(String path) {
        return path.replaceAll("^" + System.getProperty("user.home").replace(".", "\\."), "\\$HOME");
    }
    
    private static String command(ProcessBuilder pb) {
        try (
            InputStream input = pb.start().getInputStream();
            InputStreamReader stream = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            return reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static String which(String executable) {
        ProcessBuilder pb = new ProcessBuilder("which", executable);
        return command(pb);
    }
    
    public static List<String> findAll(String file) {
        return find(file, "/");
    }
    
    public static List<String> find(String file, String rootFolder) {
        ProcessBuilder pb = new ProcessBuilder("find", rootFolder, "-type", "f", "-name", file);
        try (
            InputStream input = pb.start().getInputStream();
            InputStreamReader stream = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            return reader.lines().filter(path -> !path.matches(".+/[Tt]rash/.+")).collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }
}
