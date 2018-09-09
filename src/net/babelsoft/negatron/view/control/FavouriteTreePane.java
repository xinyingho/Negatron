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
package net.babelsoft.negatron.view.control;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import net.babelsoft.negatron.controller.EditController;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.io.configuration.FavouriteTree;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Editable;
import net.babelsoft.negatron.util.function.Delegate;

/**
 *
 * @author capan
 */
public class FavouriteTreePane extends SplitPane {
    
    private FavouriteTreePaneController controller;
    
    public FavouriteTreePane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/FavouriteTreePane.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.setId(getId());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setFavouriteTree(FavouriteTree favourites) {
        controller.setFavouriteTree(favourites);
    }
    
    public void setEditController(EditController editController) {
        controller.setEditController(editController);
    }
    
    public void setEditableControl(Editable editable) {
        controller.setEditableControl(editable);
    }
    
    public boolean isEditing() {
        return controller.isEditing();
    }

    public boolean isEditingMachine() {
        return controller.isEditingMachine();
    }
    
    public boolean isEditingSoftware() {
        return controller.isEditingSoftware();
    }
    
    public boolean isEditingConfiguration() {
        return controller.isEditingConfiguration();
    }
    
    public void clearSelection() {
        controller.clearSelection();
    }
    
    public void setOnCommitted(Consumer<Favourite> onCommitted) {
        controller.setOnCommitted(onCommitted);
    }

    public boolean isCommitting() {
        return controller.isCommitting();
    }
    
    public void setOnAction(Delegate delegate) {
        controller.setOnAction(delegate);
    }

    public void insert(Machine currentMachine, SoftwareConfiguration currentSoftware) {
        controller.insert(currentMachine, currentSoftware);
    }
    
    public void cancelEdit() {
        controller.cancelEdit();
    }
    
    public void showSoftwareList() {
        controller.showSoftwareList();
    }
    
    public void hideSoftwareList() {
        controller.hideSoftwareList();
    }

    public void hideListBackground() {
        controller.hideListBackground();
    }

    public void requestTreeFocus() {
        controller.requestTreeFocus();
    }
}
