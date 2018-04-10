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

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import net.babelsoft.negatron.controller.DeviceController;
import net.babelsoft.negatron.controller.SoftwareConfigurationPaneController;
import net.babelsoft.negatron.view.control.adapter.SoftwarePartAdapter;

/**
 *
 * @author capan
 */
public class SoftwarePartChoiceCell extends TableCell<SoftwarePartAdapter, DeviceController> {

    private static final String EMPTY = "<none>";
    private static final StringConverter<DeviceController> converter;
    
    static {
        converter = new StringConverter<DeviceController>() {
            @Override
            public String toString(DeviceController device) {
                return device.getMachineComponent().getName();
            }

            @Override
            public DeviceController fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    
    private ChoiceBox<DeviceController> choiceBox;

    public SoftwarePartChoiceCell() {
        getStyleClass().add("choice-box-table-cell");
        
        textProperty().addListener((o, oV, newValue) -> {
            if (newValue == null || getIndex() < 0 || getTableView().getItems().size() <= 0)
                return;
            
            SoftwarePartAdapter currentAdapter = getTableView().getItems().get(getIndex());
            String name = currentAdapter.getName();

            currentAdapter.getDevices().stream().filter(
                device -> !device.getMachineComponent().getName().equals(newValue) && name.equals(device.getText())
            ).forEach(
                device -> device.setText("")
            );
            
            getTableView().getItems().stream().filter(
                adapter ->
                    adapter != currentAdapter &&
                    adapter.getAssignment() != null &&
                    adapter.getAssignment().getMachineComponent().getName().equals(newValue)
            ).forEach(
                adapter -> adapter.setAssignment(SoftwareConfigurationPaneController.DEFAULT_DEVICE)
            );
            
            // if the cell hasn't been updated through choicebox selections,
            // ensure that the cell content and the current choicebox selection both remain synchronised
            setItem(currentAdapter.getAssignment());
        });
    }
    
    private String getItemText() {
        DeviceController device = getItem();
        if (device != null)
            return converter.toString(device);
        else
            return EMPTY;
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable())
            return;
        
        if (choiceBox == null) {
            choiceBox = new ChoiceBox<>();
            choiceBox.setMaxWidth(Double.MAX_VALUE);
            choiceBox.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
                if (!isEditing())
                    return;
                commitEdit(newValue);
                
                String name = getTableView().getItems().get(getIndex()).getName();
                if (oldValue != null && oldValue != SoftwareConfigurationPaneController.DEFAULT_DEVICE && name.equals(oldValue.getText()))
                    oldValue.setText("");

                if (newValue != null && newValue != SoftwareConfigurationPaneController.DEFAULT_DEVICE) {
                    SoftwarePartAdapter currentAdapter = getTableView().getItems().get(getIndex());

                    getTableView().getItems().stream().filter(
                        adapter -> adapter != currentAdapter && adapter.getAssignment() == newValue
                    ).forEach(
                        adapter -> adapter.setAssignment(SoftwareConfigurationPaneController.DEFAULT_DEVICE)
                    );

                    newValue.setText(name);
                }
            });
            choiceBox.setConverter(converter);
        }
        
        choiceBox.getItems().setAll( getTableView().getItems().get(getIndex()).getDevices() );
        
        DeviceController item = getItem();
        if (item != null)
            choiceBox.getSelectionModel().select(item);
        else
            choiceBox.getSelectionModel().selectFirst();
        
        super.startEdit(); // here, the current item is always reset to none. This triggers a bug when selecting the same item twice: it gets unexpectedly deselected without raising any events
        setItem(item); // workaround for above bug
        
        setText(null);
        setGraphic(choiceBox);
        
        // instead of entering edit mode by triple-clicking, do it with a single-click (part 2/2)
        choiceBox.show();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItemText());
        setGraphic(null);
    }

    @Override
    public void updateItem(DeviceController item, boolean empty) {
        super.updateItem(item, empty);

        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else if (isEditing()) {
            if (choiceBox != null)
                choiceBox.getSelectionModel().select(getItem());
            setText(null);
            setGraphic(choiceBox);
        } else {
            setText(getItemText());
            setGraphic(null);
        }
    }
}