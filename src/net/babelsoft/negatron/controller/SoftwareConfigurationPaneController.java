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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.TableColumnConfiguration;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwarePart;
import net.babelsoft.negatron.view.control.MachineConfigurationPane;
import net.babelsoft.negatron.view.control.SoftwarePartChoiceCell;
import net.babelsoft.negatron.view.control.adapter.SoftwarePartAdapter;
import net.babelsoft.negatron.view.control.adapter.TableColumnsAdapter;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class SoftwareConfigurationPaneController implements Initializable {
    
    public static final DeviceController DEFAULT_DEVICE;
    static {
        DEFAULT_DEVICE = new DeviceController();
        DEFAULT_DEVICE.setMachineComponent(null);
    }

    @FXML
    private TableView<SoftwarePartAdapter> root;
    @FXML
    private TableColumn<SoftwarePartAdapter, String> descriptionColumn;
    @FXML
    private TableColumn<SoftwarePartAdapter, String> nameColumn;
    @FXML
    private TableColumn<SoftwarePartAdapter, DeviceController> assignmentColumn;
    
    private MachineConfigurationPane machineConfigurationPane;
    private TableColumnsAdapter<TableColumn<SoftwarePartAdapter, ?>, TableColumnConfiguration> columnsAdapter;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        columnsAdapter = new TableColumnsAdapter<>(
            root.getColumns(), root.getSortOrder(),
            (col, conf) -> col.setSortType(conf.getSortType()),
            (conf, col) -> conf.setSortType(col.getSortType()),
            TableColumnConfiguration.class,
            conf -> {
                try {
                    Configuration.Manager.updateTableColumnsConfiguration(null, conf);
                } catch (IOException ex) {
                    Logger.getLogger(EmulatedItemTreePaneController.class.getName()).log(Level.SEVERE, "Table column layout configuration couldn't be saved", ex);
                }
            }
        );
        
        loadLayout();
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        assignmentColumn.setCellValueFactory(new PropertyValueFactory<>("assignment"));
        assignmentColumn.setCellFactory(column -> new SoftwarePartChoiceCell());
        
        // instead of entering edit mode by triple-clicking, do it with a single-click (part 1/2)
        root.getSelectionModel().selectedIndexProperty().addListener((o, oV, newValue) -> {
            int row = newValue.intValue();
            if (row >= 0) Platform.runLater(() -> { // runLater is required for when selecting with the mouse, not with the keyboard
                root.getFocusModel().focus(row, assignmentColumn);
                root.edit(row, assignmentColumn);
            });
        });
    }
    
    public void setMachineConfigurationPane(MachineConfigurationPane machineConfigurationPane) {
        this.machineConfigurationPane = machineConfigurationPane;
    }

    public void setSoftware(Software software) {
        machineConfigurationPane.resetControllers();
        List<SoftwarePartAdapter> adapters = new ArrayList<>();
        
        if (software != null) software.getSoftwareParts().forEach(softwarePart -> {
            List<DeviceController> devices = new ArrayList<>();
            devices.add(DEFAULT_DEVICE);
            DeviceController[] assignedController = new DeviceController[1];

            machineConfigurationPane.getControllers().stream().filter(
                controller -> controller instanceof DeviceController
            ).map(
                controller -> (DeviceController) controller
            ).filter(
                controller -> ((Device) controller.getMachineComponent()).getInterfaceFormats().contains(
                    softwarePart.getInterfaceFormat()
                )
            ).forEach(
                controller -> {
                    devices.add(controller);
                    
                    String name = SoftwarePartAdapter.getName(software, softwarePart);
                    controller.addSoftwarePart(name);
                    if (name.equals(controller.getText()))
                        assignedController[0] = controller;
                }
            );

            SoftwarePartAdapter adapter = new SoftwarePartAdapter(software, softwarePart, devices);
            if (assignedController[0] != null)
                adapter.setAssignment(assignedController[0]);
            adapters.add(adapter);
        });
        
        root.getItems().setAll(adapters);
    }
    
    public void setCurrentItem(SoftwarePart softwarePart, String device) {
        root.getItems().stream().filter(
            adapter -> adapter.getPartName().equals(softwarePart.getName())
        ).findAny().ifPresent(adapter ->
            adapter.getDevices().stream().filter(
                deviceController -> deviceController.getMachineComponent().getName().equals(device)
            ).findAny().ifPresent(
                deviceController -> deviceController.setText(adapter.getName())
            )
        );
    }
    
    private void loadLayout() {
        columnsAdapter.loadLayout(
            Configuration.Manager.getTableColumnsConfiguration(null)
        );
        
        final Timeline resizeTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> saveColumnsLayout())
        );
        
        root.setOnSort(evt -> saveColumnsLayout());
        root.getColumns().addListener(
            (ListChangeListener.Change<? extends TableColumn<SoftwarePartAdapter, ?>> c) -> saveColumnsLayout()
        );
        root.getColumns().forEach(col -> {
            col.widthProperty().addListener((o, oV, nV) -> resizeTimeline.playFromStart());
            col.visibleProperty().addListener((o, oV, nV) -> saveColumnsLayout());
        });
    }
    
    private void saveColumnsLayout() {
        columnsAdapter.saveColumnsLayout(null);
    }

    public void requestTableFocus() {
        root.requestFocus();
    }
}
