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

import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.Infotip;
import net.babelsoft.negatron.view.control.ListSelectionView;

/**
 *
 * @author capan
 */
public class PluginSelectionField extends Field {
    
    protected ListSelectionView<String> selection;
    
    public PluginSelectionField(GridPane grid, int row, String disabledPluginKey, String enabledPluginKey) {
        selection = new ListSelectionView<>();
        
        Configuration.Manager.retrievePlugins(
                disabledPluginKey, enabledPluginKey
        ).forEach(plugin -> {
            if (plugin.isEnabled())
                selection.getTargetItems().add(plugin.getName());
            else
                selection.getSourceItems().add(plugin.getName());
        });
        
        selection.getSourceLabel().setText(Language.Manager.getString("globalConf." + disabledPluginKey));
        selection.getTargetLabel().setText(Language.Manager.getString("globalConf." + enabledPluginKey));
        selection.setSourceTooltip(new Infotip(Language.Manager.getString("globalConf." + disabledPluginKey + ".tooltip")));
        selection.setTargetTooltip(new Infotip(Language.Manager.getString("globalConf." + enabledPluginKey + ".tooltip")));
        selection.getSourceItems().addListener((Observable o) -> updatePlugins(disabledPluginKey, enabledPluginKey, selection.getSourceItems(), false));
        selection.getTargetItems().addListener((Observable o) -> updatePlugins(disabledPluginKey, enabledPluginKey, selection.getTargetItems(), true));
        
        GridPane.setColumnSpan(selection, 2);
        grid.add(selection, 0, row);
    }
    
    private void updatePlugins(String disabledPluginKey, String enabledPluginKey, ObservableList<String> list, boolean updateEnabled) {
        updatePlugins(
                disabledPluginKey, enabledPluginKey, list.stream().sorted().collect(Collectors.joining(",")), updateEnabled
        );
    }
    
    public void setDisabled(boolean value) {
        selection.setDisable(value);
    }
}
