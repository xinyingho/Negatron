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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import net.babelsoft.negatron.controller.SoftwareConfigurationPaneController;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwarePart;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.adapter.SoftwarePartAdapter;

/**
 *
 * @author capan
 */
public class SoftwareConfigurationPane extends TableView<SoftwarePartAdapter> {
    
    private SoftwareConfigurationPaneController controller;
    
    public SoftwareConfigurationPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/SoftwareConfigurationPane.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void setMachineConfigurationPane(MachineConfigurationPane machineConfigurationPane) {
        controller.setMachineConfigurationPane(machineConfigurationPane);
    }
    
    public void setSoftware(Software software) {
        controller.setSoftware(software);
    }

    public void setCurrentItem(SoftwarePart softwarePart, String device) {
        controller.setCurrentItem(softwarePart, device);
    }
    
    public void requestTableFocus() {
        controller.requestTableFocus();
    }
}
