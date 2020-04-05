/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2020 BabelSoft S.A.S.U.
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

import com.eclipsesource.json.Json;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.Infotip;
import net.babelsoft.negatron.view.control.ListSelectionView;
import net.babelsoft.negatron.view.skin.ListSelectionViewSkin;

/**
 *
 * @author Xiny
 */
public class PluginSelectionField extends Field {
    
    protected class Plugin {
        final String name;
        final boolean enabledByDefault;
        
        Plugin(String name, boolean enabledByDefault) {
            this.name = name;
            this.enabledByDefault = enabledByDefault;
        }
    }
    
    protected ListSelectionView<String> selection;
    
    public PluginSelectionField(GridPane grid, int row, String disabledPluginKey, String enabledPluginKey, List<Path> paths) {
        final List<String> disabledPlugins = Arrays.asList(
                Configuration.Manager.getGlobalConfiguration(disabledPluginKey).split(",")
        );
        final List<String> enabledPlugins = Arrays.asList(
                Configuration.Manager.getGlobalConfiguration(enabledPluginKey).split(",")
        );
        selection = new ListSelectionView<>();
        
        paths.stream().flatMap(path -> {
            try {
                return Files.list(path);
            } catch (IOException ex) {
                Logger.getLogger(PluginSelectionField.class.getName()).log(
                        Level.WARNING, String.format("Couldn't list plugins folders within the folder %s", path), ex
                );
                return null;
            }
        }).filter(
                path -> Files.isDirectory(path)
        ).map(pluginFolder -> {
            try (Reader fr = new FileReader(pluginFolder.resolve("plugin.json").toFile())) {
                return Json.parse(fr).asObject().get("plugin").asObject();
            } catch (Exception ex) {
                Logger.getLogger(PluginSelectionField.class.getName()).log(
                        Level.WARNING, String.format("Couldn't retrieve plugin.json file within the folder %s", pluginFolder), ex
                );
                return null;
            }
        }).filter(
                plugin -> plugin != null && plugin.getString("type", "").equals("plugin") && !plugin.getString("name", "").isBlank()
        ).map(
                plugin -> new Plugin(plugin.getString("name", null), Boolean.parseBoolean( plugin.getString("start", "false") ))
        ).forEach(plugin -> {
            if (plugin.enabledByDefault) {
                if (disabledPlugins.contains(plugin.name))
                    selection.getSourceItems().add(plugin.name);
                else
                    selection.getTargetItems().add(plugin.name);
            } else {
                if (enabledPlugins.contains(plugin.name))
                    selection.getTargetItems().add(plugin.name);
                else
                    selection.getSourceItems().add(plugin.name);
            }
        });
        
        selection.getSourceLabel().setText(Language.Manager.getString("globalConf." + disabledPluginKey));
        selection.getTargetLabel().setText(Language.Manager.getString("globalConf." + enabledPluginKey));
        selection.setSourceTooltip(new Infotip(Language.Manager.getString("globalConf." + disabledPluginKey + ".tooltip")));
        selection.setTargetTooltip(new Infotip(Language.Manager.getString("globalConf." + enabledPluginKey + ".tooltip")));
        selection.getSourceItems().addListener((Observable o) -> updatePluginSetting(disabledPluginKey, selection.getSourceItems()));
        selection.getTargetItems().addListener((Observable o) -> updatePluginSetting(enabledPluginKey, selection.getTargetItems()));
        
        GridPane.setColumnSpan(selection, 2);
        grid.add(selection, 0, row);
    }
    
    private void updatePluginSetting(String key, ObservableList<String> list) {
        updateGlobalConfigurationSetting(
                key, list.stream().sorted().collect(Collectors.joining(","))
        );
        
        // update plugin.ini
        updatePlugins(selection.getSourceItems(), selection.getTargetItems());
    }
    
    public void setDisabled(boolean value) {
        selection.setDisable(value);
    }
}
