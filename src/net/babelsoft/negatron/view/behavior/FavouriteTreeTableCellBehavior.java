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
package net.babelsoft.negatron.view.behavior;

import com.sun.javafx.scene.control.behavior.TreeTableCellBehavior;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;

/**
 *
 * @author capan
 */
public class FavouriteTreeTableCellBehavior<S,T> extends TreeTableCellBehavior<S,T> {
    
    public FavouriteTreeTableCellBehavior(TreeTableCell<S,T> control) {
        super(control);
    }
    
    @Override
    protected void handleClicks(MouseButton button, int clickCount, boolean isAlreadySelected) {
        // handle editing, which only occurs with the middle mouse button
        TreeItem<S> treeItem = getNode().getTreeTableRow().getTreeItem();
        if (button == MouseButton.MIDDLE && clickCount == 1 && isAlreadySelected) {
            edit(getNode());
        } else if ((button == MouseButton.PRIMARY || button == MouseButton.MIDDLE) && clickCount == 1) {
            // cancel editing
            edit(null);
        } else if (button == MouseButton.MIDDLE && clickCount == 2 && treeItem.isLeaf()) {
            // attempt to edit
            edit(getNode());
        } else if (button == MouseButton.MIDDLE && clickCount % 2 == 0) {
            // try to expand/collapse branch tree item
            treeItem.setExpanded(! treeItem.isExpanded());
        }
    }
}
