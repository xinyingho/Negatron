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
package net.babelsoft.negatron.view.control;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import javafx.beans.NamedArg;
import javafx.scene.control.Spinner;

/**
 *
 * @author capan
 */
public class YearSpinner extends Spinner<Integer> {
    
    public YearSpinner(
        @NamedArg("min") int min
    ) {
        this(min, LocalDate.now().get(ChronoField.YEAR_OF_ERA));
    }
    
    public YearSpinner(
        @NamedArg("min") int min,
        @NamedArg("initialValue") int initialValue
    ) {
        this(min, LocalDate.now().get(ChronoField.YEAR_OF_ERA), initialValue);
    }
    
    public YearSpinner(
        @NamedArg("min") int min,
        @NamedArg("max") int max,
        @NamedArg("initialValue") int initialValue
    ) {
        super(min, max, initialValue);
        
        String defaultValue = Integer.toString(initialValue);
        
        getEditor().textProperty().addListener((o, oV, newValue) -> {
            try {
                Integer.valueOf(newValue);
            } catch (NumberFormatException | NullPointerException ex) {
                getEditor().setText(defaultValue);
            }
        });
    }
}
