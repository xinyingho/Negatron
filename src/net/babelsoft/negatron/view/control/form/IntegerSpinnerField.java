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

import java.util.IllegalFormatConversionException;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class IntegerSpinnerField extends Field {
    
    protected Spinner<Integer> spinner;
    
    public IntegerSpinnerField(GridPane grid, int row, String key, int maxValue) {
        this(grid, row, key, 0, maxValue, 1);
    }
    
    public IntegerSpinnerField(GridPane grid, int row, String key, int minValue, int maxValue, int step) {
        Label label = new Label(Language.Manager.getString("globalConf." + key));
        grid.add(label, 0, row);
        
        Tooltip tooltip = new Tooltip(Language.Manager.tryGetString("globalConf." + key + ".tooltip"));
        
        String sValue = Configuration.Manager.getGlobalConfiguration(key);
        int currentValue = Strings.isValid(sValue) ? Integer.parseInt(sValue) : 0;
        spinner = new Spinner<>(minValue, maxValue, currentValue, step);
        // by default, user must type on Enter to validate a modified value when typing it instead of using the up / down arrows.
        // here, set up Negatron to automatically save any modifications.
        spinner.getEditor().focusedProperty().addListener((o, oV, newValue) -> {
            if (!newValue)
                spinner.getValueFactory().setValue(Integer.valueOf(spinner.getEditor().getText()));
        });
        spinner.setEditable(true);
        spinner.setTooltip(tooltip);
        grid.add(spinner, 1, row);
        
        spinner.valueProperty().addListener((o, oV, newValue) -> {
            try {
                updateGlobalConfigurationSetting(key, String.format("%d", newValue));
            } catch (IllegalFormatConversionException ex) {

            }
        });
    }
}
