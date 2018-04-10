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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.babelsoft.negatron.model.favourites.Favourite;

/**
 *
 * @author capan
 */
public class CopyPastableTreeItem extends SortableTreeItem<Favourite> {
    
    public static enum CutCopyState {
        None,
        Cut,
        Copied
    }
    
    private final ObjectProperty<CutCopyState> cutCopyState = new SimpleObjectProperty<>();
    
    public CopyPastableTreeItem() {
        super(null);
    }
    
    public CopyPastableTreeItem(Favourite value) {
        super(value);
        value.setChildren(getChildren());
    }

    public CutCopyState getCutCopyState() {
        return cutCopyState.get();
    }

    public void setCutCopyState(CutCopyState value) {
        cutCopyState.set(value);
    }

    public ObjectProperty<CutCopyState> cutCopyStateProperty() {
        return cutCopyState;
    }
    
    public CopyPastableTreeItem copy() {
        CopyPastableTreeItem copied = new CopyPastableTreeItem(getValue().copy());
        getInternalChildren().stream().map(
            child -> (CopyPastableTreeItem) child
        ).forEach(child -> {
            CopyPastableTreeItem copiedChild = child.copy();
            copied.getInternalChildren().add(copiedChild);
        });
        return copied;
    }
}
