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

import java.util.AbstractList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author capan
 */
public final class Dom {
    
    private Dom() {}
    
    public static List<Node> list(final NodeList list) {
        return new AbstractList<Node>() {
            
            @Override
            public int size() {
                return list.getLength();
            }

            @Override
            public Node get(int index) {
                Node item = list.item(index);
                if (item == null)
                    throw new IndexOutOfBoundsException();
                return item;
            }
        };
    }
   
    public static List<Node> getElementsByTagName(Element e, String tag) {
        return list(e.getElementsByTagName(tag));
    }
}
