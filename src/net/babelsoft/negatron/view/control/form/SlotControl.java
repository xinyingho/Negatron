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
package net.babelsoft.negatron.view.control.form;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.controller.SlotController;
import net.babelsoft.negatron.model.comparing.Difference;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class SlotControl extends VBox implements Control<SlotController> {
    
    private SlotController controller;
    private Difference status;
    
    public SlotControl(Slot slot, Difference status) {
        this.status = status;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/net/babelsoft/negatron/view/control/fxml/SlotControl.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.setMachineComponent(slot);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    @Override
    public SlotController getController() {
        return controller;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Difference getStatus() {
        return status;
    }
}
