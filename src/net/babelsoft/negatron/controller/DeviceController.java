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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.HddCreationDialog;
import net.babelsoft.negatron.view.control.SoftwareConfigurationPane;
import net.babelsoft.negatron.view.control.adapter.FocusData;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class DeviceController extends MachineComponentController<Device, String, StringProperty> {
    
    private static final Device DEFAULT_DEVICE = new Device("<none>", null, null, false);
    private static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");
    private static final KeyEvent SUBMIT_TEXT = new KeyEvent(KeyEvent.KEY_RELEASED, null, null, KeyCode.ENTER, false, false, false, false);
    
    private static AlertController alertController;
    private static SoftwareConfigurationPane softwareConfigurationPane;
    
    public static void setAlertController(AlertController alertController) {
        DeviceController.alertController = alertController;
    }
    
    public static void setSoftwareConfigurationPane(SoftwareConfigurationPane softwareConfigurationPane) {
        DeviceController.softwareConfigurationPane = softwareConfigurationPane;
    }
    
    @FXML
    private HBox root;
    @FXML
    private TextField text;
    @FXML
    private ComboBox<String> combo;
    @FXML
    private Button createButton;
    @FXML
    private ToggleButton listButton;
    
    private ChangeListener<String> textListener;
    private ChangeListener<String> valueListener;
    
    private boolean isText() {
        return text.getParent() != null;
    }

    @Override
    public void dispose() {
        Device device = getMachineComponent();
        if (device != DEFAULT_DEVICE) {
            text.textProperty().unbindBidirectional(device.valueProperty());
            text.textProperty().removeListener(textListener);
            combo.valueProperty().unbindBidirectional(device.valueProperty());
            combo.valueProperty().removeListener(valueListener);
        }
    }

    public void reset() {
        if (!isText()) {
            root.getChildren().add(0, text);
            root.getChildren().remove(combo);
            
            String value = combo.getValue();
            combo.getItems().clear(); // this also clears the value, contrary to what javadoc says, and so also the content of the textfield through bindings...
            text.setText(value); // workaround for above remark
        }
    }
    
    @Override
    public void setMachineComponent(Device device) {
        if (device == null) {
            super.setMachineComponent(DEFAULT_DEVICE);
            return;
        }
        
        super.setMachineComponent(device);
        
        // Text
        text.textProperty().bindBidirectional(device.valueProperty());
        textListener = (o, oV, newValue) -> {
            if (device.isMandatory())
                if (newValue.isEmpty())
                    text.pseudoClassStateChanged(ERROR_CLASS, true);
                else
                    text.pseudoClassStateChanged(ERROR_CLASS, false);
            fireDataUpdated(getMachineComponent().getName());
        };
        text.textProperty().addListener(textListener);
        
        if (device.isMandatory() && Strings.isEmpty(text.getText()))
            text.pseudoClassStateChanged(ERROR_CLASS, true);
        
        // Combo
        combo.valueProperty().bindBidirectional(device.valueProperty());
        valueListener = (o, oV, newValue) -> {
            if (device.isMandatory())
                if (newValue.isEmpty())
                    combo.pseudoClassStateChanged(ERROR_CLASS, true);
                else
                    combo.pseudoClassStateChanged(ERROR_CLASS, false);
            
            softwareConfigurationPane.getItems().stream().filter(
                adapter -> adapter.getDevices().contains(this)
            ).forEach(adapter -> {
                if (adapter.getName().equals(newValue))
                    adapter.setAssignment(this);
                else if (adapter.getAssignment() == this)
                    adapter.setAssignment(SoftwareConfigurationPaneController.DEFAULT_DEVICE);
                // context synchronisation with other devices is handled in SoftwarePartChoiceCell
            });
        };
        combo.valueProperty().addListener(valueListener);
        
        if (device.isMandatory() && Strings.isEmpty(combo.getValue()))
            combo.pseudoClassStateChanged(ERROR_CLASS, true);
        
        combo.focusedProperty().addListener((o, oV, newValue) -> {
            // when the combobox lost the focus, automatically submit any modified text instead of forcing users to press the Enter key to commit modified text
            String value = combo.getValue();
            String text = combo.getEditor().getText();
            if (!newValue && (value == null && !text.isEmpty() || value != null && !value.equals(text)))
                combo.fireEvent(SUBMIT_TEXT);
        });
        root.getChildren().remove(combo);
        
        // Buttons
        if (!device.getType().equals("harddisk")) {
            ((Pane) createButton.getParent()).getChildren().remove(createButton);
            createButton = null;
        }
        
        if (!device.hasCompatibleSoftwareLists()) {
            ((Pane) listButton.getParent()).getChildren().remove(listButton);
            listButton = null;
        }
        
        device.getExtensions().add("*.zip");
    }

    @Override
    public boolean isFocused() {
        if (isText())
            return text.isFocused();
        else
            return combo.isFocused();
    }
    
    @Override
    public FocusData getFocusData() {
        int pos = isText() ? text.getCaretPosition() : combo.getEditor().getCaretPosition();
        return new FocusData(getMachineComponent(), pos);
    }

    @Override
    public boolean requestFocus(FocusData focusData) {
        if (super.requestFocus(focusData)) {
            if (isText()) {
                text.requestFocus();
                text.positionCaret(focusData.getCaretPosition());
            } else {
                combo.requestFocus();
                combo.getEditor().positionCaret(focusData.getCaretPosition());
            }
            return true;
        } else
            return false;
    }
    
    public boolean setSoftware(Software software) {
        if (!software.getName().equals(getText())) {
            setText(software.getName());
            return true;
        } else
            return false;
    }

    public void addSoftwarePart(String softwarePartName) {
        if (isText()) {
            root.getChildren().remove(text);
            root.getChildren().add(0, combo);
        }
        combo.getItems().add(softwarePartName);
    }
    
    public void setText(String text) {
        if (getMachineComponent() == DEFAULT_DEVICE)
            return;
        
        if (isText())
            this.text.setText(text);
        else
            combo.setValue(text);
    }
    
    public String getText() {
        if (getMachineComponent() == DEFAULT_DEVICE)
            return "";
        
        if (isText())
            return text.getText();
        else
            return combo.getValue();
    }
    
    public StringProperty valueProperty() {
        return getMachineComponent().valueProperty();
    }
    
    public void addSoftwareListActionHandler(EventHandler<ActionEvent> value) {
        if (listButton != null)
            listButton.setOnAction(value);
    }
    
    public void setListButtonSelected(boolean state) {
        if (listButton != null)
            listButton.setSelected(state);
    }
    
    public boolean isListButtonSelected() {
        return listButton.isSelected();
    }
    
    public void showList() {
        if (listButton != null && !listButton.isSelected())
            listButton.fire();
    }

    public void hideList() {
        if (listButton != null && listButton.isSelected())
            listButton.fire();
    }
    
    private void handleResetAction(ActionEvent event) {
        if (isText())
            text.clear();
        else
            combo.setValue(null);
    }
    
    @FXML
    private void handleCreateAction(ActionEvent event) {
        if (Strings.isValid(Configuration.Manager.getChdmanExecutable()))
            new HddCreationDialog(root.getScene().getWindow()).showAndWait().ifPresent(hddGeometry -> {
                try {
                    Mame.createBlankHdd(hddGeometry);
                    setText(hddGeometry.getPath());
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(DeviceController.class.getName()).log(Level.SEVERE, null, ex);
                    alertController.alert(AlertType.ERROR, "Couldn't create blank HDD image:" + ex.getLocalizedMessage());
                }
            });
        else
            alertController.alert(AlertType.ERROR,
                "Couldn't initiate the blank HDD creation dialog: " +
                "Path to the tool CHDMAN is not valid. Please, fix this in the global configuration pane.\n\n" +
                "If you installed a packaged MAME on Linux, you may also need to install the optional mame-tools package."
            );
    }
    
    @FXML
    private void handleBrowseAction(ActionEvent event) {
        Control c = (Control) event.getSource();
        FileChooser fc = new FileChooser();
        
        String description;
        Iterator<String> it = getMachineComponent().getInterfaceFormats().iterator();
        if (it.hasNext()) {
            description = it.next();
            while (it.hasNext())
                description += " / " + it.next();
        } else
            description = getMachineComponent().getName();
        
        fc.getExtensionFilters().addAll(
            new ExtensionFilter(
                description, getMachineComponent().getExtensions()
            ),
            new ExtensionFilter("All Files", "*.*")
        );
        File f = fc.showOpenDialog(c.getScene().getWindow());
        
        if (f != null)
            if (isText())
                text.setText(f.getAbsolutePath());
            else
                combo.setValue(f.getAbsolutePath());
    }
}
