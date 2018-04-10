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

import javafx.scene.control.TableColumn.SortType;

/**
 *
 * @author capan
 */
public class TableColumnConfiguration extends TableColumnBaseConfiguration<SortType> {
    static final long serialVersionUID = 1L;
    
    public TableColumnConfiguration() { super(); }
    
    public TableColumnConfiguration(String name, int order, boolean sorted, int sortRank, SortType sortType, boolean visible, double width) {
        super(name, order, sorted, sortRank, sortType, visible, width);
    }
}
