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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public abstract class InteractiveTreeTableCell<I> extends FavouriteTreeTableCell<I> {
    
    private static final String OK = ButtonType.OK.getText();
    public static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");
    
    protected I editField;
    protected VBox editPane;
    protected Label label;
    protected Button button;
    
    public InteractiveTreeTableCell(FavouriteTreePaneController controller) {
        super(controller);
    }
    
    protected void createButton() {
        button = new Button(OK);
        button.setOnAction(event -> commitRowEdit());
    }
    
    protected boolean createEditPane() {
        if (editPane == null) {
            label = new Label();
            createButton();
            editPane = new VBox(label, button);
            editPane.setAlignment(Pos.CENTER);
            editPane.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                    t.consume();
                }
            });
            return true;
        } else
            return false;
    }
    
    protected abstract void showInteractivePane();
    protected abstract void hideInteractivePane();
    
    protected String initEdit() {
        editField = getItem();
        return null;
    }
    
    private void initRowEdit() {
        getTreeTableRow().getChildrenUnmodifiable().stream().filter(
            cell -> cell != this && cell instanceof InteractiveTreeTableCell
        ).map(
            cell -> (InteractiveTreeTableCell) cell
        ).forEach(
            cell -> cell.initEdit()
        );
        controller.show(getTreeTableRow().getTreeItem());
    }
    
    public abstract void setEdit(Machine machine, SoftwareConfiguration softwareConfiguration, MachineConfiguration machineConfiguration);
    
    /** {@inheritDoc} */
    @Override
    public void startEdit() {
        super.startEdit();

        if (isEditing()) {
            createEditPane();
            
            label.setText(initEdit());
            initRowEdit();
            
            CellUtils.startEdit(this, editPane);
            showInteractivePane();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void cancelEdit() {
        if (isEditing()) {
            super.cancelEdit();
            hideInteractivePane();
            
            cancelRowEdit();
            controller.show(getTreeTableRow().getTreeItem());
        } else
            super.cancelEdit();
    }
    
    private void cancelRowEdit() {
        getTreeTableRow().getChildrenUnmodifiable().stream().filter(
            cell -> cell != this && cell instanceof InteractiveTreeTableCell
        ).map(
            cell -> (InteractiveTreeTableCell) cell
        ).forEach(
            cell -> cell.cancelEdit()
        );
    }

    public void commitEdit() {
        if (isEditing())
            commitEdit(editField);
        else
            updateItem(getTreeTableRow().getItem());
        editField = null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void commitEdit(I newValue) {
        controller.setCommitting(true);
        hideInteractivePane();
        super.commitEdit(newValue);
        controller.setCommitting(false);
    }
    
    private void commitRowEdit() {
        Favourite fav = getTreeTableRow().getItem();
        if (fav.mustMigrate())
            fav.setMustMigrate(false);
        
        getTreeTableRow().getChildrenUnmodifiable().stream().filter(
            cell -> cell != this && cell instanceof InteractiveTreeTableCell
        ).map(
            cell -> (InteractiveTreeTableCell) cell
        ).forEach(cell -> {
            cell.pseudoClassStateChanged(ERROR_CLASS, false);
            cell.commitEdit();
        });
        // commit the editing cell at the end so that the favourite saving process
        // has all the required information
        commitEdit();
        
        controller.fireOnCommitted();
    }

    /** {@inheritDoc} */
    @Override
    protected void updateItem(I item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
            pseudoClassStateChanged(ERROR_CLASS, false);
        } else {
            Node separator = getSeparator();
            if (separator == null) {
                setText(toString());
                
                Favourite fav = getTreeTableRow().getItem();
                if (fav != null && fav.mustMigrate())
                    pseudoClassStateChanged(ERROR_CLASS, true);
                else
                    pseudoClassStateChanged(ERROR_CLASS, false);
            } else {
                setText(null);
                pseudoClassStateChanged(ERROR_CLASS, false);
            }
            setGraphic(separator);
        }
    }
    
    protected abstract void updateItem(Favourite item);
}
