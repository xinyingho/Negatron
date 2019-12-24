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

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import net.babelsoft.negatron.view.control.Infotip;

/**
 *
 * @author capan
 */
public class ComboField<T> extends Field {
    
    protected final ComboBox<T> comboBox;
    
    public ComboField(GridPane grid, int row, String text, String prompt) {
        Label label = new Label(text);
        grid.add(label, 0, row);
        
        comboBox = new ComboBox<>();
        comboBox.setTooltip(new Infotip(prompt));
        comboBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(comboBox, 1, row);
        GridPane.setHgrow(comboBox, Priority.SOMETIMES);

        // add dummy constraints for current row
        RowConstraints constraints = new RowConstraints();
        grid.getRowConstraints().add(constraints);
    }
}
