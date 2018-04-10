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
package net.babelsoft.negatron.io.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author capan
 */
public class TreeTableViewConfiguration implements Serializable {
    static final long serialVersionUID = 1L;
    
    private boolean flatten;
    private Map<String, TreeTableColumnConfiguration> layout; // column id > column conf
    
    public TreeTableViewConfiguration() {
        flatten = false;
        layout = new HashMap<>();
    }

    /**
     * @return the flatten
     */
    public boolean isFlatten() {
        return flatten;
    }

    /**
     * @return the layout
     */
    public Map<String, TreeTableColumnConfiguration> getLayout() {
        return layout;
    }

    /**
     * @param flatten the flatten to set
     */
    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    /**
     * @param layout the layout to set
     */
    public void setLayout(Map<String, TreeTableColumnConfiguration> layout) {
        this.layout = layout;
    }
}
