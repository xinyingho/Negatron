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
package net.babelsoft.negatron.view.control.form;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.configuration.VsyncMethod;

/**
 *
 * @author capan
 */
public abstract class Field {
    
    private static int rowCount;
    
    protected static int getRowCount() {
        return rowCount++;
    }
    
    public static void resetRowCount() {
        rowCount = 0;
    }
    
    private void alert(Exception ex) {
        Logger.getLogger(Field.class.getName()).log(Level.SEVERE, null, ex);
        Alert alert = new Alert(
            Alert.AlertType.ERROR,
            "Couldn't save configuration. Negatron may not have the write permission on mame.ini or negatron.ini, or those files may be read-only.",
            ButtonType.OK
        );
        alert.show();
    }
    
    protected void updateMamePath(String path) {
        try {
            Configuration.Manager.updateMamePath(path);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    protected void updateMameIni(String path) {
        try {
            Configuration.Manager.updateMameIni(path);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    protected void updateChdmanPath(String path) {
        try {
            Configuration.Manager.updateChdmanPath(path);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    /**
     * 
     * @param path
     * @param domain
     * @return if a current transaction ended
     */
    protected boolean updateMasterConfigPath(Domain domain, String path) {
        try {
            return Configuration.Manager.updateMasterConfigPath(domain, path);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
            return true;
        }
    }
    
    protected void updateVlcPath(String path) {
        try {
            Configuration.Manager.updateVlcPath(path);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    protected void updateSkin(String name) {
        try {
            Configuration.Manager.updateSkin(name);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    protected void updateInfotipTiming(String name) {
        try {
            Configuration.Manager.updateInfotipTiming(name);
        } catch (IOException ex) {
            alert(ex);
        }
    }
    
    protected void updateCheatMenuEnabled(boolean cheatMenuEnabled) {
        try {
            Configuration.Manager.updateCheatMenuEnabled(cheatMenuEnabled);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateVsyncMethod(VsyncMethod vsync) {
        try {
            Configuration.Manager.updateVsyncMethod(vsync);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateFont(String family, double size) {
        try {
            Configuration.Manager.updateFont(family, size);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateFolderPath(Property property, int index, String path) {
        try {
            Configuration.Manager.updateFolderPath(property, index, path);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateFilePath(Property property, int index, String path, String charSet) {
        try {
            Configuration.Manager.updateFilePath(property, index, path, charSet);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateMachinePrimaryPath(Property property, String text) {
        try {
            Configuration.Manager.updateMachinePrimaryPath(property, text);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateSoftwarePrimaryPath(Property property, String text) {
        try {
            Configuration.Manager.updateSoftwarePrimaryPath(property, text);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateGlobalConfigurationSetting(String key, String value) {
        try {
            Configuration.Manager.updateGlobalConfigurationSetting(key, value);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void updateGlobalConfigurationSetting(String key, boolean value) {
        try {
            Configuration.Manager.updateGlobalConfigurationSetting(key, value);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void removePath(Property property, int index) {
        try {
            Configuration.Manager.removePath(property, index);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
    
    protected void commitMasterConfigTransaction(Domain domain) {
        try {
            Configuration.Manager.commitMasterConfigTransaction(domain);
        } catch (IOException | InterruptedException ex) {
            alert(ex);
        }
    }
}
