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
package net.babelsoft.negatron.model;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author capan
 */
public enum ScreenOrientation {
    NONE,
    HORIZONTAL("0", "180"),
    VERTICAL("90", "270");
    
    private static final List<ScreenOrientation> enumValues = Arrays.asList(ScreenOrientation.values());
    private final List<String> values;
    
    ScreenOrientation(String... values) {
        this.values = Arrays.asList(values);
    }
    
    public boolean isCompatible(String value) {
        return values.contains(value);
    }
    
    public static ScreenOrientation getValue(String value) {
        return enumValues.stream().filter(
            orientation -> orientation.isCompatible(value)
        ).findAny().orElse(NONE);
    }
}
