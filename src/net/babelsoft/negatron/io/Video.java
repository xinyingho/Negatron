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
package net.babelsoft.negatron.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.util.PathUtil;
import net.babelsoft.negatron.util.Strings;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

/**
 *
 * @author capan
 */
public class Video {
    
    private Video() { }
    
    public static boolean isEnabled() {
        // disable bogus X11 multithreading context initialisation, which isn't needed anymore with VLC 2.2.0+
        // otherwise it'll unexpectedly raise a race condition and a native crash when trying to open a folder selection dialogbox
        // System.setProperty("VLCJ_INITX", "no");
        return new NativeDiscovery().discover();
    }
    
    public static class UserHintedDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {

        @Override
        public int priority() {
            return 1;
        }

        @Override
        public String[] directories() {
            String vlcPath = Configuration.Manager.getVlcPath();
            
            if (Strings.isValid(vlcPath)) try {
                String[] directoryNames = new String[1];
                directoryNames[0] = Paths.get(vlcPath).getParent().toString();
                return directoryNames;
            } catch (Exception ex) { } // swallow errors
            
            return new String[0];
        }

        @Override
        public boolean supported() {
            return true;
        }
    }
    
    public static class LinuxDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {
        @Override
        public int priority() {
            return 0;
        }

        @Override
        public String[] directories() {
            /*List<String> directoryNames = new ArrayList<>();
            directoryNames.add("/usr/lib64");
            directoryNames.add("/usr/local/lib64");
            return directoryNames.toArray(new String[0]);*/
            return new String[0];
        }

        @Override
        public boolean supported() {
            return RuntimeUtil.isNix();
        }
    }
    
    public static class WindowsDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {

        @Override
        public int priority() {
            return 2;
        }

        @Override
        public String[] directories() {
            String vlcPath = Configuration.Manager.getVlcPath();
            if (Strings.isEmpty(vlcPath) || Files.notExists(Paths.get(vlcPath))) try {
                // detect any Windows versions of VLC media player included in Negatron's installation folder
                Path libVlc = Files.find(Paths.get("."), 2,
                    (p, bfa) -> {
                        return p.toString().startsWith(".\\vlc-") && p.endsWith("libvlc.dll") && bfa.isRegularFile();
                    }
                ).findAny().orElse(null);
                
                if (libVlc == null)
                    libVlc = PathUtil.retrieveFromJavaLibraryPaths("vlc", "libvlc.dll");
                    
                if (libVlc != null)
                    Configuration.Manager.updateVlcPath(libVlc.toString());
                    // then VLC gets detected by UserHintedDiscoveryDirectoryProvider
            } catch (Exception ex) {
                Logger.getLogger(Video.class.getName()).log(
                    Level.WARNING,
                    "Failed to detect any portable versions of VLC included in Negatron's installation folder",
                    ex
                );
            }
            return new String[0];
        }

        @Override
        public boolean supported() {
            return RuntimeUtil.isWindows();
        }
    }
    
    public static class OsxDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {

        @Override
        public int priority() {
            return 2;
        }

        @Override
        public String[] directories() {
            String vlcPath = Configuration.Manager.getVlcPath();
            if (Strings.isEmpty(vlcPath) || Files.notExists(Paths.get(vlcPath))) try {
                // detect any macOS versions of VLC media player included in Negatron's installation folder
                Path libVlc = Paths.get("VLC.app/Contents/MacOS/lib/libvlc.dylib");
                if (!Files.exists(libVlc))
                    libVlc = PathUtil.retrieveFromJavaLibraryPaths("VLC.app", "Contents", "MacOS", "lib", "libvlc.dylib");
                if (libVlc != null)
                    Configuration.Manager.updateVlcPath(libVlc.toString());
                    // then VLC gets detected by UserHintedDiscoveryDirectoryProvider
            } catch (Exception ex) {
                Logger.getLogger(Video.class.getName()).log(
                    Level.WARNING,
                    "Failed to detect any versions of VLC included in Negatron's installation folder",
                    ex
                );
            }
            return new String[0];
        }

        @Override
        public boolean supported() {
            return RuntimeUtil.isMac();
        }
    }
}
