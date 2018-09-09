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
package net.babelsoft.negatron.controller;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.model.component.Bios;
import net.babelsoft.negatron.model.component.BiosSet;
import net.babelsoft.negatron.model.component.Choice;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.model.component.SlotOption;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class SlotController extends ChoiceController<SlotOption> {
    
    private InvalidationListener biosChoiceBoxListener;
    private Bios currentBios;
        
    @FXML
    private VBox root;
    @FXML
    private HBox biosHBox;
    @FXML
    private ChoiceBox<BiosSet> biosChoiceBox;
    @FXML
    private Button biosDefaultButton;
    
    @Override
    public void setMachineComponent(Choice<SlotOption> choice) {
        super.setMachineComponent(choice);
        biosChoiceBox.getItems().clear();
        
        Slot slot = (Slot) choice;
        SlotOption option = slot.getValue();
        if (option != null) {
            Machine subdevice = option.getDevice();
            if (subdevice != null) {
                currentBios = subdevice.getBios();
                if (currentBios != null) {
                    biosChoiceBox.getItems().addAll(currentBios.getOptions());
                    
                    if (slot.getBios() != null)
                        biosChoiceBox.getSelectionModel().select(currentBios.getOptions().stream().filter(
                            b -> b.getName().equals(slot.getBios())
                        ).findAny().get());
                    else // default bios
                        biosChoiceBox.getSelectionModel().select(currentBios.getValue());
                    
                    biosChoiceBoxListener = o -> {
                        BiosSet set = biosChoiceBox.getSelectionModel().getSelectedItem();
                        Slot component = (Slot) getMachineComponent();
                        if (component.getBios() == null || !component.getBios().equals(set.getName())) {
                            component.setBios(set);
                            fireDataUpdated(component.getName());
                        }
                    };
                    biosChoiceBox.getSelectionModel().selectedItemProperty().addListener(biosChoiceBoxListener);
        
                    if (biosHBox.getParent() == null)
                        root.getChildren().add(biosHBox);
                    return;
                }
            }
        }
        
        if (biosHBox.getParent() != null)
            root.getChildren().remove(biosHBox);
        currentBios = null;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (biosChoiceBoxListener != null)
            biosChoiceBox.getSelectionModel().selectedItemProperty().removeListener(biosChoiceBoxListener);
    }
    
    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        biosChoiceBox.setDisable(!editable);
        biosDefaultButton.setVisible(editable);
    }
    
    @FXML
    private void handleDefaultBiosAction(ActionEvent event) {
        biosChoiceBox.getSelectionModel().select(currentBios.getDefaultValue());
    }
}
