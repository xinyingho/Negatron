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
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public abstract class NumberField<T extends Number> extends Field {
    
    private final HBox pane;
    protected Slider slider;
    protected Spinner<T> spinner;
    
    protected boolean updating;
    
    protected NumberField() {
        pane = new HBox(5.0);
    }
    
    public void initialise(GridPane grid, int row, String key, String format) {
        Label label = new Label(Language.Manager.getString("globalConf." + key));
        grid.add(label, 0, row);
        
        Tooltip tooltip = new Tooltip(Language.Manager.tryGetString("globalConf." + key + ".tooltip"));
        
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setTooltip(tooltip);
        
        spinner.setEditable(true);
        spinner.setTooltip(tooltip);
        
        pane.getChildren().addAll(slider, spinner);
        HBox.setHgrow(slider, Priority.SOMETIMES);
        grid.add(pane, 1, row);
        
        spinner.valueProperty().addListener((o, oV, newValue) -> {
            if (!updating) {
                updating = true;
                slider.setValue(newValue.doubleValue());
                try {
                    updateGlobalConfigurationSetting(key, String.format(format, newValue));
                } catch (IllegalFormatConversionException ex) {
                    
                }
                updating = false;
            }
        });
    }
    
    public void setDisable(boolean value) {
        slider.setDisable(value);
        spinner.setDisable(value);
    }
}
