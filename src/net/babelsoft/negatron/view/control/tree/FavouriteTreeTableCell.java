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

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeTableView;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.favourites.Favourite;

/**
 *
 * @author capan
 */
public class FavouriteTreeTableCell<I> extends NegatronTreeTableCell<Favourite, I> {
    
    public static final PseudoClass EDITABLE_CLASS = PseudoClass.getPseudoClass("editable");
    
    protected FavouriteTreePaneController controller;
    
    public FavouriteTreeTableCell(FavouriteTreePaneController controller) {
        super();
        setAlignment(Pos.CENTER_LEFT);
        this.controller = controller;
        
        setOnMouseEntered(e -> controller.setHoveredCell(this));
        setOnMouseMoved(e -> {
            if (!e.isAltDown() && getTableRow().isSelected() && !isEditing() && canEdit())
                pseudoClassStateChanged(EDITABLE_CLASS, true);
            else
                pseudoClassStateChanged(EDITABLE_CLASS, false);
        });
    }
    
    private boolean isSeparator() {
        return getTableRow().getItem() instanceof net.babelsoft.negatron.model.favourites.Separator;
    }
    
    protected Node getSeparator() {
        Separator separator = null;
        if (isSeparator()) {
            separator = new Separator();
            separator.setMouseTransparent(true);
        }
        return separator;
    }
    
    public boolean canEdit() {
        return getItem() != null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void startEdit() {
        // If a cell is marked as non-editable, it looses the ability to cancel the editing of an adjacent cell when clicking on it
        // So all the cells remain marked as editable, and the below test is made with canEdit() instead of isEditable()
        if (canEdit()) {
            super.startEdit();
            controller.setEditingCell(this);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        controller.setEditingCell(null);
    }
    
    /** {@inheritDoc} */
    @Override
    public void commitEdit(I newValue) {
        // super.commitEdit() updates the cell content, which loses the focus in the process,
        // then tries to put the focus on the parent of the henceforth non-existent focused element...
        // So the below code fixes this by pre-emptively passing the focus to the parent.
        final TreeTableView<Favourite> table = getTreeTableView();
        if (table != null)
            CellUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
        
        super.commitEdit(newValue);
        controller.setEditingCell(null);
        controller.saveConfiguration();
        
        Favourite favourite = getTableRow().getItem();
        if (favourite.isInvalidated())
            favourite.checkValidity();
    }
}
