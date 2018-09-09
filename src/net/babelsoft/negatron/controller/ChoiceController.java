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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import net.babelsoft.negatron.model.Option;
import net.babelsoft.negatron.model.OptionProperty;
import net.babelsoft.negatron.model.component.Choice;
import net.babelsoft.negatron.view.control.adapter.FocusData;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class ChoiceController<T extends Option<T>> extends MachineComponentController<Choice<T>, T, OptionProperty<T>> {
    
    private InvalidationListener choiceListener;
    private InvalidationListener choiceBoxListener;
    
    @FXML
    private ChoiceBox<T> choiceBox;
    @FXML
    private Button defaultButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { }
    
    @Override
    public void setMachineComponent(Choice<T> choice) {
        super.setMachineComponent(choice);
        choiceBox.getItems().addAll(choice.getOptions());
        
        // since selectedItemProperty() is read-only, have to do this workaround for bidirectional binding
        choiceBox.getSelectionModel().select(choice.getValue());
        
        choiceListener = o -> {
            SingleSelectionModel<T> selectionModel = choiceBox.getSelectionModel();
            T option = getMachineComponent().getValue();
            if (selectionModel.getSelectedItem() != option)
                choiceBox.getSelectionModel().select(option);
        };
        choice.valueProperty().addListener(choiceListener);
        
        choiceBoxListener = o -> {
            T option = choiceBox.getSelectionModel().getSelectedItem();
            Choice<T> component = getMachineComponent();
            if (component.getValue() != option) {
                component.setValue(option);
                fireDataUpdated(component.getName());
            }
        };
        choiceBox.getSelectionModel().selectedItemProperty().addListener(choiceBoxListener);
    }

    @Override
    public void dispose() {
        getMachineComponent().valueProperty().removeListener(choiceListener);
        choiceBox.getSelectionModel().selectedItemProperty().removeListener(choiceBoxListener);
    }

    @Override
    public boolean isFocused() {
        return choiceBox.isFocused();
    }

    @Override
    public FocusData getFocusData() {
        return new FocusData(getMachineComponent());
    }

    @Override
    public boolean requestFocus(FocusData focusData) {
        if (super.requestFocus(focusData)) {
            choiceBox.requestFocus();
            return true;
        } else
            return false;
    }
    
    @Override
    public void setEditable(boolean editable) {
        choiceBox.setDisable(!editable);
        defaultButton.setVisible(editable);
    }

    @FXML
    private void handleDefaultAction(ActionEvent event) {
        choiceBox.getSelectionModel().select(getMachineComponent().getDefaultValue());
    }
}
