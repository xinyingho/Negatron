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

import java.util.function.Function;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.Describable;
import net.babelsoft.negatron.model.favourites.Favourite;

/**
 *
 * @author capan
 */
public abstract class DescribableTreeTableCell<T extends Describable> extends InteractiveTreeTableCell<T> {
    
    private static final PseudoClass CSS_INVALID = PseudoClass.getPseudoClass("invalid");
    
    public DescribableTreeTableCell(FavouriteTreePaneController controller, Function<Favourite, Boolean> isCellInvalid) {
        super(controller);
        ChangeListener<Boolean> invalidatedListener = (o, oV, invalidated) -> {
            if (invalidated && isCellInvalid.apply(getTableRow().getItem()))
                pseudoClassStateChanged(CSS_INVALID, true);
            else
                pseudoClassStateChanged(CSS_INVALID, false);
        };
        ChangeListener<Favourite> favouriteListener = (o, oldFav, newFav) -> {
            if (newFav != null && newFav.invalidatedProperty() != null) {
                newFav.invalidatedProperty().addListener(invalidatedListener);
                invalidatedListener.changed(null, Boolean.FALSE, newFav.isInvalidated());
            }
            if (oldFav != null && oldFav.invalidatedProperty() != null)
                oldFav.invalidatedProperty().removeListener(invalidatedListener);
        };
        this.tableRowProperty().addListener((o, oldRow, newRow) -> {
            if (newRow != null) {
                newRow.itemProperty().addListener(favouriteListener);
                favouriteListener.changed(null, null, newRow.getItem());
            }
            if (oldRow != null)
                oldRow.itemProperty().removeListener(favouriteListener);
        });
    }
    
    @Override
    protected String initEdit() {
        super.initEdit();
        return toString();
    }

    /** {@inheritDoc} */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        
        setText(toString());
        setGraphic(null);
    }
    
    @Override
    public String toString() {
        T item = getItem();
        if (item != null)
            return item.getDescription();
        else
            return null;
    }
}
