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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.babelsoft.negatron.io.configuration.TableColumnConfiguration;
import net.babelsoft.negatron.io.configuration.TreeTableColumnConfiguration;
import net.babelsoft.negatron.io.configuration.TreeTableViewConfiguration;

/**
 *
 * @author capan
 */
public class UIConfigurationCache extends Cache<UIConfigurationCache.Data, Void> {
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    protected static class Data implements Serializable {
        static final long serialVersionUID = 5L;
        
        protected Map<String, TreeTableViewConfiguration> treeTableConfiguration; // tree table view id > tree table view conf
        protected Map<String, TableColumnConfiguration> tableConfiguration; // column id > column conf
        protected double mainDividerPosition;
        protected UIConfigurationData filterConfiguration;
        protected Map<String, UIConfigurationData> filterConfigurationMap;
        protected int machineInformationTabIndex;
        protected int softwareInformationTabIndex;
        protected int globalConfigurationTabIndex;
        protected boolean loopEnabled;
        protected int windowWidth, windowHeight;
        protected boolean isWindowMaximised, isWindowFullscreen;
        protected String selectedMachineFolderView;
        protected Map<String, Void> machineFoldersRemovedFomView; // folder removed from view > dummy data
        protected boolean isGlobalAdvancedOptionsEnabled;
        
        public Data() {
            treeTableConfiguration = new HashMap<>();
            tableConfiguration = new HashMap<>();
            mainDividerPosition = -1;
            filterConfiguration = new UIConfigurationData();
            filterConfigurationMap = new HashMap<>();
            machineInformationTabIndex = 0;
            softwareInformationTabIndex = 0;
            globalConfigurationTabIndex = 0;
            loopEnabled = false;
            windowWidth = DEFAULT_WIDTH;
            windowHeight = DEFAULT_HEIGHT;
            isWindowMaximised = isWindowFullscreen = false;
            machineFoldersRemovedFomView = new HashMap<>();
            isGlobalAdvancedOptionsEnabled = false;
        }
    }
    
    private Data data;
    private boolean transaction;
    
    public UIConfigurationCache() throws ClassNotFoundException, IOException {
        super("ui");
        
        try {
            data = load();
        } catch (ClassNotFoundException|IOException ex) { }
        
        if (data != null) {
            if (data.windowWidth < 10 || data.windowHeight < 10) {
                data.windowWidth = DEFAULT_WIDTH;
                data.windowHeight = DEFAULT_HEIGHT;
            }
            if (data.machineFoldersRemovedFomView == null)
                data.machineFoldersRemovedFomView = new HashMap<>();
        } else
            data = new Data();
    }
    
    private <T> T loadTreeTableViewConfiguration(String id, Function<TreeTableViewConfiguration, T> get, T defaultValue) {
        TreeTableViewConfiguration conf = data.treeTableConfiguration.get(id);
        if (conf != null)
            return get.apply(conf);
        else
            return defaultValue;
    }
    
    public Map<String, TreeTableColumnConfiguration> loadTreeTableColumnsConfiguration(String id) {
        return loadTreeTableViewConfiguration(id, conf -> conf.getLayout(), null);
    }
    
    public boolean loadTreeTableFlattenConfiguration(String id) {
        return loadTreeTableViewConfiguration(id, conf -> conf.isFlatten(), false);
    }
    
    private void saveTreeTableViewConfiguration(String id, Consumer<TreeTableViewConfiguration> updateViewConfiguration) throws IOException {
        TreeTableViewConfiguration vconf = data.treeTableConfiguration.get(id);
        if (vconf == null)
            vconf = new TreeTableViewConfiguration();
        updateViewConfiguration.accept(vconf);
        
        data.treeTableConfiguration.put(id, vconf);
        save(data);
    }
    
    public void saveTreeTableColumnsConfiguration(String id, Map<String, TreeTableColumnConfiguration> conf) throws IOException {
        saveTreeTableViewConfiguration(id, vconf -> vconf.setLayout(conf));
    }
    
    public void saveTreeTableFlattenConfiguration(String id, boolean flatten) throws IOException {
        saveTreeTableViewConfiguration(id, vconf -> vconf.setFlatten(flatten));
    }
    
    public Map<String, TableColumnConfiguration> loadTableColumnsConfiguration(String id) {
        // Negatron currently only has a single TableView, so id is useless for now
        return data.tableConfiguration;
    }

    public void saveTableColumnsConfiguration(String id, Map<String, TableColumnConfiguration> conf) throws IOException {
        // Negatron currently only has a single TableView, so id is useless for now
        data.tableConfiguration = conf;
        save(data);
    }

    public double loadMainDividerPosition() {
        return data.mainDividerPosition;
    }
    
    public void saveMainDividerPosition(double position) throws IOException {
        data.mainDividerPosition = position;
        save(data);
    }

    public int loadMachineInformationTabIndex() {
        return data.machineInformationTabIndex;
    }

    public void saveMachineInformationTabIndex(int index) throws IOException {
        data.machineInformationTabIndex = index;
        save(data);
    }

    public int loadSoftwareInformationTabIndex() {
        return data.softwareInformationTabIndex;
    }

    public void saveSoftwareInformationTabIndex(int index) throws IOException {
        data.softwareInformationTabIndex = index;
        save(data);
    }

    public int loadGlobalConfigurationTabIndex() {
        return data.globalConfigurationTabIndex;
    }

    public void saveGlobalConfigurationTabIndex(int index) throws IOException {
        data.globalConfigurationTabIndex = index;
        save(data);
    }
    
    public UIConfigurationData loadFilterConfiguration() {
        return data.filterConfiguration;
    }

    public UIConfigurationData loadFilterConfiguration(String key) {
        if (data.filterConfigurationMap.containsKey(key))
            return data.filterConfigurationMap.get(key);
        else
            return new UIConfigurationData();
    }
    
    public void saveFilterConfiguration(UIConfigurationData data) throws IOException {
        this.data.filterConfiguration = data;
        save(this.data);
    }

    public void saveFilterConfiguration(String key, UIConfigurationData data) throws IOException {
        this.data.filterConfigurationMap.put(key, data);
        save(this.data);
    }

    public boolean loadLoopEnabled() {
        return data.loopEnabled;
    }

    public void saveLoopEnabled(boolean value) throws IOException {
        data.loopEnabled = value;
        save(data);
    }
    
    public int loadWindowWidth() {
        return data.windowWidth;
    }
    
    public void saveWindowWidth(int value) throws IOException {
        data.windowWidth = value;
        save(data);
    }
    
    public int loadWindowHeight() {
        return data.windowHeight;
    }
    
    public void saveWindowHeight(int value) throws IOException {
        data.windowHeight = value;
        save(data);
    }

    public boolean loadWindowMaximised() {
        return data.isWindowMaximised;
    }

    public void saveWindowMaximised(boolean value) throws IOException {
        data.isWindowMaximised = value;
        save(data);
    }

    public boolean loadWindowFullscreen() {
        return data.isWindowFullscreen;
    }

    public void saveWindowFullscreen(boolean value) throws IOException {
        data.isWindowFullscreen = value;
        save(data);
    }
    
    public String loadSelectedMachineFolderView() {
        return data.selectedMachineFolderView;
    }
    
    public void saveSelectedMachineFolderView(String value) throws IOException {
        data.selectedMachineFolderView = value;
        data.machineFoldersRemovedFomView.clear();
        save(data);
    }
    
    public Map<String, Void> loadMachineFoldersRemovedFromView() {
        return data.machineFoldersRemovedFomView;
    }
    
    public void saveMachineFolderRemovedFromView(String value) throws IOException {
        data.machineFoldersRemovedFomView.put(value, null);
        if (!transaction)
            save(data);
    }
    
    public void saveMachineFolderAddedIntoView(String value) throws IOException {
        data.machineFoldersRemovedFomView.remove(value);
        if (!transaction)
            save(data);
    }

    public boolean loadGlobalAdvancedOptionsEnabled() {
        return data.isGlobalAdvancedOptionsEnabled;
    }

    public void saveGlobalAdvancedOptionsEnabled(boolean value) throws IOException {
        data.isGlobalAdvancedOptionsEnabled = value;
        save(data);
    }
    
    public void beginTransaction() {
        transaction = true;
    }
    
    public void endTransaction() throws IOException {
        transaction = false;
        save(data);
    }
}
