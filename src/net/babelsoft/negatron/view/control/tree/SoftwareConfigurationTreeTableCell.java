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

import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class SoftwareConfigurationTreeTableCell extends DescribableTreeTableCell<SoftwareConfiguration> {
    
    public SoftwareConfigurationTreeTableCell(FavouriteTreePaneController controller) {
        super(controller, favourite -> favourite.isSoftwareInvalid());
    }

    @Override
    protected void showInteractivePane() {
        controller.showSoftwareList();
    }

    @Override
    protected void hideInteractivePane() {
        controller.hideSoftwareList();
    }
    
    @Override
    public void setEdit(Machine machine, SoftwareConfiguration softwareConfiguration, MachineConfiguration machineConfiguration) {
        String text = softwareConfiguration != null ? softwareConfiguration.getDescription() : null;
        if (isEditing())
            label.setText(text);
        else
            setText(text);
        editField = softwareConfiguration;
    }
    
    @Override
    protected void updateItem(Favourite item) {
        item.setSoftwareConfiguration(editField);
    }
}
