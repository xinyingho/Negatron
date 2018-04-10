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
package net.babelsoft.negatron.util;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 *
 * @author capan
 */
public final class Debug {
    
    private Debug() { }

    public static void dump(Node n) { dump(n, 0); }
    
    private static void dump(Node n, int depth) {
        for (int i = 0; i < depth; ++i)
            System.out.print("  ");
        
        System.out.println(n);
        
        if (n instanceof Parent)
            ((Parent) n).getChildrenUnmodifiable().stream().forEach(
                c -> dump(c, depth + 1)
            );
    }
}
