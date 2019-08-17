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

import java.util.Arrays;
import java.util.List;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.OSD;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Shell;

/**
 *
 * @author capan
 */
public class OSDChoiceField<T extends OSD> extends ChoiceField<T> {
    
    public OSDChoiceField(GridPane grid, int row, String key, T[] values) {
        super(
            grid, row, 
            Language.Manager.getString("globalConf." + key),
            Language.Manager.tryGetString("globalConf." + key + ".tooltip")
        );
        
        List<T> list = Arrays.asList(values);
        list.stream().filter(item ->
            item.isWindowsCompatible() && Shell.isWindows() ||
            item.isMacCompatible() && Shell.isMacOs() ||
            item.isLinuxCompatible() && Shell.isLinux()
        ).forEach(
            item -> choiceBox.getItems().add(item)
        );
        
        String init = Configuration.Manager.getGlobalConfiguration(key);
        list.stream().filter(
            constant -> init.equals(constant.getName())
        ).findAny().ifPresent(
            constant -> choiceBox.getSelectionModel().select(constant)
        );
        
        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
            updateGlobalConfigurationSetting(key, newValue.getName());
        });
    }
}
