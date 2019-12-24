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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.Infotip;

/**
 *
 * @author capan
 */
public class IpAddressField extends Field {
    
    private final HBox pane;
    protected List<Spinner<Integer>> spinners;
    
    public IpAddressField(GridPane grid, int row, String key) {
        Label label = new Label(Language.Manager.getString("globalConf." + key));
        grid.add(label, 0, row);
        
        Infotip tooltip = new Infotip(Language.Manager.tryGetString("globalConf." + key + ".tooltip"));
        
        pane = new HBox(5.0);
        grid.add(pane, 1, row);
        
        spinners = new ArrayList<Spinner<Integer>>(4);
        String[] currentValue = Configuration.Manager.getGlobalConfiguration(key).split("\\.");
        for (int i = 0;i < 4;++i) {
            int spinnerValue = Integer.parseInt(currentValue[i]);
            
            Spinner<Integer> spinner = new Spinner<>(0, 255, spinnerValue, 1);
            // by default, user must type on Enter to validate a modified value when typing it instead of using the up / down arrows.
            // here, set up Negatron to automatically save any modifications.
            spinner.getEditor().focusedProperty().addListener((o, oV, newValue) -> {
                if (!newValue)
                    spinner.getValueFactory().setValue(Integer.valueOf(spinner.getEditor().getText()));
            });
            spinner.setEditable(true);
            spinner.setTooltip(tooltip);
        
            spinner.valueProperty().addListener((o, oV, newValue) -> {
                String ipValue = spinners.stream().map(
                    field -> field.getValue().toString()
                ).collect(Collectors.joining("."));
                updateGlobalConfigurationSetting(key, ipValue);
            });
        
            HBox.setHgrow(spinner, Priority.SOMETIMES);
            pane.getChildren().add(spinner);
            spinners.add(spinner);
        }
    }
}
