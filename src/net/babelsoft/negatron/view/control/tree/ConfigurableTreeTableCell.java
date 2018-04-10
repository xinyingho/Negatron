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

import javafx.scene.image.ImageView;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;

/**
 *
 * @author capan
 */
public class ConfigurableTreeTableCell<T extends EmulatedItem<T>> extends NegatronTreeTableCell<T, Boolean> {
    
    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (empty || item == null || item == false)
            setGraphic(null);
        else
            setGraphic(new ImageView( ((EmulatedItemTreeView<T>) getTreeTableView()).getConfigurationIcon() ));
    }
}
