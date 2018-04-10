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
package net.babelsoft.negatron.view.control.adapter;

import javafx.util.StringConverter;
import net.babelsoft.negatron.model.IconDescription;

/**
 *
 * @author capan
 */
public class IconDescriptionStringConverter extends StringConverter<IconDescription> {
    
    private IconDescription buffer;

    /** {@inheritDoc} */
    @Override
    public String toString(IconDescription value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        }

        buffer = value;
        return value.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public IconDescription fromString(String value) {
        // If the specified value is null or zero-length, return null
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.length() < 1) {
            return null;
        }

        IconDescription res = new IconDescription(value, buffer.getIcon());
        buffer = null;
        return res;
    }
}
