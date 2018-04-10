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
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.util.Strings;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.discovery.linux.DefaultLinuxNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.mac.DefaultMacNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.windows.DefaultWindowsNativeDiscoveryStrategy;

/**
 *
 * @author capan
 */
public class Video {
    
    private Video() { }
    
    public static boolean isEnabled() {
        // disable bogus X11 multithreading context initialisation, which isn't needed anymore with VLC 2.2.0+
        // otherwise it'll unexpectedly raise a race condition and a native crash when trying to open a folder selection dialogbox
        System.setProperty("VLCJ_INITX", "no");
        return new CustomNativeDiscovery().discover();
    }
    
    private static class CustomNativeDiscovery extends NativeDiscovery {

        /**
         * Create a discovery component with the default platform strategies.
         */
        public CustomNativeDiscovery() {
            super(
                new CustomLinuxNativeDiscoveryStrategy(),
                new CustomWindowsNativeDiscoveryStrategy(),
                new CustomMacNativeDiscoveryStrategy()
            );
        }
    }
    
    private static interface UserHintedDiscoveryStrategy {
        
        default void onGetUserDirectoryNames(List<String> directoryNames) {
            String vlcPath = Configuration.Manager.getVlcPath();
            if (Strings.isValid(vlcPath)) try {
                directoryNames.add(
                    Paths.get(vlcPath).getParent().toString()
                );
            } catch (Exception ex) { } // swallow errors
        }
    }
    
    private static class CustomLinuxNativeDiscoveryStrategy extends DefaultLinuxNativeDiscoveryStrategy implements UserHintedDiscoveryStrategy {

        @Override
        protected void onGetDirectoryNames(List<String> directoryNames) {
            onGetUserDirectoryNames(directoryNames);
            
            super.onGetDirectoryNames(directoryNames);
            
            directoryNames.add("/usr/lib64");
            directoryNames.add("/usr/local/lib64");
        }
    }
    
    private static class CustomWindowsNativeDiscoveryStrategy extends DefaultWindowsNativeDiscoveryStrategy implements UserHintedDiscoveryStrategy {
        
        @Override
        protected void onGetDirectoryNames(List<String> directoryNames) {
            String vlcPath = Configuration.Manager.getVlcPath();
            if (Strings.isEmpty(vlcPath)) try {
                // detect any Windows versions of VLC media player included in Negatron's installation folder
                final String libVlc = "VLC\\libvlc.dll";
                if (Files.exists(Paths.get(libVlc)))
                    Configuration.Manager.updateVlcPath(libVlc);
            } catch (Exception ex) {
                Logger.getLogger(Video.class.getName()).log(
                    Level.WARNING,
                    "Failed to detect any portable versions of VLC included in Negatron's installation folder",
                    ex
                );
            }
            
            onGetUserDirectoryNames(directoryNames);
            super.onGetDirectoryNames(directoryNames);
        }
    }
    
    private static class CustomMacNativeDiscoveryStrategy extends DefaultMacNativeDiscoveryStrategy implements UserHintedDiscoveryStrategy {
        
        @Override
        protected void onGetDirectoryNames(List<String> directoryNames) {
            String vlcPath = Configuration.Manager.getVlcPath();
            if (Strings.isEmpty(vlcPath)) try {
                // detect any macOS versions of VLC media player included in Negatron's installation folder
                final String libVlc = "VLC.app/Contents/MacOS/lib/libvlc.dylib";
                if (Files.exists(Paths.get(libVlc)))
                    Configuration.Manager.updateVlcPath(libVlc);
            } catch (Exception ex) {
                Logger.getLogger(Video.class.getName()).log(
                    Level.WARNING,
                    "Failed to detect any versions of VLC included in Negatron's installation folder",
                    ex
                );
            }
            
            onGetUserDirectoryNames(directoryNames);
            super.onGetDirectoryNames(directoryNames);
        }
    }
}
