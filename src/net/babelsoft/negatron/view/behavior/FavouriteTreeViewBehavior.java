/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2024 BabelSoft S.A.S.U.
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
package net.babelsoft.negatron.view.behavior;

import com.sun.javafx.scene.control.behavior.TreeTableViewBehavior;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableColumn;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.view.control.FavouriteTreeView;

/**
 *
 * @author Xiny
 */
public class FavouriteTreeViewBehavior extends TreeTableViewBehavior<Favourite> {
    
    public FavouriteTreeViewBehavior(FavouriteTreeView control) {
        super(control);
    }
    
    /** {@inheritDoc}  */
    @Override 
    @SuppressWarnings("unchecked")
    protected void editCell(int row, TableColumnBase tc) {
        ((FavouriteTreeView) getNode()).editCell(row, (TreeTableColumn<Favourite, ?>)tc);
    }
}
