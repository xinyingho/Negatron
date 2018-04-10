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
package net.babelsoft.negatron.view.control.tree;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author capan
 */
public abstract class NegatronTreeTableCell<T, I> extends TreeTableCell<T, I> {
    
    public NegatronTreeTableCell() {
        super();
        
        // Disable node expansion/collapsing on double-clicking
        addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            TreeItem<T> treeItem = getTreeTableRow().getTreeItem();
            if (event.getButton() == MouseButton.PRIMARY && treeItem != null && !treeItem.isLeaf() && event.getClickCount() % 2 == 0)
                event.consume();
        });
    }
}
