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

import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class LocalisedChoiceField<T extends Enum> extends ValueChoiceField<T> {
    
    public LocalisedChoiceField(GridPane grid, int row, String key, T[] values) {
        super(grid, row, key, values);
        
        choiceBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                // decapitalise enum class name
                String name = object.getClass().getSimpleName();
                char c[] = name.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                name = new String(c);
                
                return Language.Manager.getString(name + "." + object.toString());
            }

            @Override
            public T fromString(String string) {
                throw new UnsupportedOperationException("Should never be called.");
            }
        });
    }
}
