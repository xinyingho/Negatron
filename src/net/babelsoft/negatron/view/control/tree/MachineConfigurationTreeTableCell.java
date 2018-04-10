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

import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.AdvancedParametrisationDialog;

/**
 *
 * @author capan
 */
public class MachineConfigurationTreeTableCell extends InteractiveTreeTableCell<MachineConfiguration> {
    
    private boolean previousIsCommandLine;

    public MachineConfigurationTreeTableCell(FavouriteTreePaneController controller) {
        super(controller);
    }
    
    @Override
    protected void createButton() {
        if (getItem().isCommandLine()) {
            button = new Button(Language.Manager.getString("advanced..."));
            button.setOnAction(event -> {
                Dialog<String> dialog = new AdvancedParametrisationDialog(
                    getTreeTableView().getScene().getWindow(), CellUtils.getItemText(this)
                );
                Optional<String> res = dialog.showAndWait();

                if (res.isPresent())
                    commitEdit(new MachineConfiguration(res.get()));
                else
                    cancelEdit();
                event.consume();
            });
        } else
            super.createButton();
    }
    
    @Override
    protected boolean createEditPane() {
        if (!super.createEditPane() && previousIsCommandLine != getItem().isCommandLine()) {
            editPane.getChildren().remove(button);
            createButton();
            editPane.getChildren().add(button);
        }
        previousIsCommandLine = getItem().isCommandLine();
        return true;
    }

    @Override
    protected void showInteractivePane() {
        controller.showConfigurationPane();
    }

    @Override
    protected void hideInteractivePane() {
        controller.hideConfigurationPane();
    }
    
    @Override
    protected String initEdit() {
        super.initEdit();
        return toString();
    }
    
    @Override
    public void setEdit(Machine machine, SoftwareConfiguration softwareConfiguration, MachineConfiguration machineConfiguration) {
        if (isEditing())
            label.setText(toString(machineConfiguration));
        else
            setText(toString(machineConfiguration));
        editField = machineConfiguration;
    }
    
    @Override
    public boolean canEdit() {
        return super.canEdit() && getItem().isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(toString());
        setGraphic(null);
    }
    
    @Override
    protected void updateItem(Favourite item) {
        item.setMachineConfiguration(editField);
    }
    
    public String toString(MachineConfiguration conf) {
        if (conf == null)
            return null;
        else if (conf.isCommandLine())
            return conf.getCommandLine();
        else
            return conf.getParameters().toString();
    }
    
    @Override
    public String toString() {
        return toString(getItem());
    }
}
