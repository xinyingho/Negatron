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

import java.util.HashMap;
import java.util.Map;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class SizeUnit {
    private static final Map<Integer, String> UNITS;
    
    static {
        UNITS = new HashMap<>();
        UNITS.put(0, "");
        UNITS.put(1, Language.Manager.getString("kiloPrefix"));
        UNITS.put(2, Language.Manager.getString("megaPrefix"));
        UNITS.put(3, Language.Manager.getString("gigaPrefix"));
        UNITS.put(4, Language.Manager.getString("teraPrefix"));
        UNITS.put(5, Language.Manager.getString("petaPrefix"));
        UNITS.put(6, Language.Manager.getString("exaPrefix"));
        UNITS.put(7, Language.Manager.getString("zettaPrefix"));
        UNITS.put(8, Language.Manager.getString("yottaPrefix"));
    }
    
    public static String get(int index) {
        return " " + UNITS.get(index) + Language.Manager.getString("byteUnit");
    }
    
    public static String factorise(long value) {
        double temp = value;
        
        int i = 0;
        do {
            ++i;
            temp /= 1024.0;
        } while (temp >= 1.0);
        if (--i > 0)
            value /= Math.pow(1024, i);

        return Long.toString(value) + SizeUnit.get(i);
    }
}
