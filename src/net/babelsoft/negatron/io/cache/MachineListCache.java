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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.statistics.MachineStatistics;

/**
 *
 * @author capan
 */
public class MachineListCache extends Cache<MachineListCache.Data, String> {
    
    public static class Data extends ArrayList<Machine> {
        static final long serialVersionUID = 3L;
        
        private final MachineStatistics statistics = new MachineStatistics();
        
        // TODO: override other add methods?
        @Override
        public boolean add(Machine machine) {
            statistics.add(machine);
            return super.add(machine);
        }
        
        public MachineStatistics getStatistics() {
            return statistics;
        }
    }

    public MachineListCache() throws ClassNotFoundException, IOException {
        super("machine");
    }

    public boolean checkVersion() throws IOException {
        if (version == null)
            return false;
        else
            return version.equals(retrieveVersion());
    }

    public String retrieveVersion() throws IOException {
        try (
            InputStream input = Mame.newInputStream("-h");
            InputStreamReader stream = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            String currentVersion = reader.readLine();
            if (currentVersion == null)
                throw new IOException("MAME didn't output anything");
            currentVersion = currentVersion.trim();
            if (
                !currentVersion.startsWith("MAME ") && // MAME >= 0.170
                !currentVersion.startsWith("M.A.M.E.") && // MAME <= 0.169
                !currentVersion.startsWith("M.E.S.S.") && // MESS < 0.162
                !currentVersion.startsWith("HBMAME ") && // Homebrew MAME >= 0.170
                !currentVersion.startsWith("HB.M.A.M.E. ") // Homebrew MAME <= 0.169
            ) throw new IOException("Configured MAME executable didn't return a valid MAME signature");
            
            return currentVersion;
        }
    }
    
    @Override
    protected String loadVersion() throws ClassNotFoundException, IOException {
        String _version = null;
        try {
            _version = super.loadVersion();
        } catch (Exception ex) {
            Logger.getLogger(MachineListCache.class.getName()).log(Level.WARNING, null, ex);
        }
        return _version;
    }
    
    @Override
    public void save(Data content) throws IOException {
        super.save(content);
        
        if (version == null)
            version = retrieveVersion();
        saveVersion();
    }

    public void clear() throws IOException {
        Files.deleteIfExists(cachePath);
        Files.deleteIfExists(versionPath);
    }
}
