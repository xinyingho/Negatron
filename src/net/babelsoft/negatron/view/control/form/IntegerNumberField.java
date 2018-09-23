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

import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;

/**
 *
 * @author capan
 */
public class IntegerNumberField extends NumberField<Integer> {
    
    private final static String format = "%d";
    
    public IntegerNumberField(GridPane grid, int row, String key, int minValue, int maxValue, int majorTickUnit, int minorTickCount, int step) {
        int currentValue = Integer.parseInt(Configuration.Manager.getGlobalConfiguration(key));
        
        slider = new Slider(minValue, maxValue, currentValue);
        slider.setMajorTickUnit(majorTickUnit);
        slider.setMinorTickCount(minorTickCount);
        slider.setSnapToTicks(true);
        slider.valueProperty().addListener((o, oV, newValue) -> {
            if (!updating) {
                updating = true;
                
                int value = newValue.intValue();
                spinner.getValueFactory().setValue(value);
                updateGlobalConfigurationSetting(key, String.format(format, value));
                
                updating = false;
            }
        });

        spinner = new Spinner<>(minValue, maxValue, currentValue, step);
        spinner.getEditor().focusedProperty().addListener((o, oV, newValue) -> {
            if (!newValue)
                spinner.getValueFactory().setValue(Integer.valueOf(spinner.getEditor().getText()));
        });
        
        initialise(grid, row, key, format);
    }
}
