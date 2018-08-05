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

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class NumberField extends Field {
    
    private final HBox pane;
    private final Slider slider;
    private final Spinner<Double> spinner;
    
    private boolean updating;
    
    public NumberField(GridPane grid, int row, String key, String format, double minValue, double maxValue, double majorTickUnit, int minorTickCount, double step) {
        Label label = new Label(Language.Manager.getString("globalConf." + key));
        grid.add(label, 0, row);
        
        double currentValue = Double.parseDouble(Configuration.Manager.getGlobalConfiguration(key));
        
        slider = new Slider(minValue, maxValue, currentValue);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(majorTickUnit);
        slider.setMinorTickCount(minorTickCount);
        slider.setTooltip(new Tooltip(Language.Manager.tryGetString("globalConf." + key + ".tooltip")));
        
        spinner = new Spinner<>(minValue, maxValue, currentValue, step);
        spinner.setEditable(true);
        
        pane = new HBox(5.0);
        pane.getChildren().addAll(slider, spinner);
        HBox.setHgrow(slider, Priority.SOMETIMES);
        grid.add(pane, 1, row);
        
        slider.valueProperty().addListener((o, oV, newValue) -> {
            if (!updating) {
                updating = true;
                spinner.getValueFactory().setValue(newValue.doubleValue());
                updateGlobalConfigurationSetting(key, String.format(format, newValue));
                updating = false;
            }
        });
        spinner.valueProperty().addListener((o, oV, newValue) -> {
            if (!updating) {
                updating = true;
                slider.setValue(newValue);
                updateGlobalConfigurationSetting(key, String.format(format, newValue));
                updating = false;
            }
        });
        spinner.getEditor().focusedProperty().addListener((o, oV, newValue) -> {
            if (!newValue.booleanValue())
                spinner.getValueFactory().setValue(Double.valueOf(spinner.getEditor().getText()));
        });
    }
}
