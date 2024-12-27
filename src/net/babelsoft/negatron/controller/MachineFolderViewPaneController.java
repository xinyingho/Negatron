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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.MachineFolder;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class MachineFolderViewPaneController implements Initializable {

    @FXML
    private ChoiceBox<IniPath> choice;
    @FXML
    private Button selectAllButton;
    @FXML
    private Button selectNoneButton;
    @FXML
    private FlowPane flow;
    
    private class IniPath {
        private final Path path;
        private final String label;
        
        public IniPath(Path path) {
            this.path = path;
            label = path.getFileName().toString();
        }
        
        public IniPath(String label) {
            path = null;
            this.label = label;
        }
        
        public Path getPath() {
            return path;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    private IniPath defaultPath;
    private Consumer<Map<SortableTreeItem<Machine>, List<String>>> onFolderViewTypeChanged;
    private BiConsumer<SortableTreeItem<Machine>, Boolean> onCheckAction;
    private boolean initialising;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        defaultPath = new IniPath(Language.Manager.getString("parentClone"));
        choice.getItems().add(defaultPath);
        
        Configuration.Manager.getFolderPaths(Property.FOLDER_VIEW).stream().map(
            path -> Paths.get(path)
        ).filter(
            path -> Files.exists(path)
        ).forEachOrdered(path -> { try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(
                iniPath -> iniPath.getFileName().toString().endsWith(".ini")
            ).forEachOrdered(
                iniPath -> choice.getItems().add(new IniPath(iniPath))
            );
        } catch (IOException ex) {
            Logger.getLogger(MachineFolderViewPaneController.class.getName()).log(Level.SEVERE, "Error while initialising view pane", ex);
            // swallow exceptions
        }});
    }

    public void initialiseData() {
        initialising = true;
        
        String selectedMachineFolderView = Configuration.Manager.getSelectedMachineFolderView();
        if (Strings.isValid(selectedMachineFolderView))
            choice.getItems().stream().filter(
                item -> item.toString().equals(selectedMachineFolderView)
            ).findAny().ifPresent(
                item -> choice.getSelectionModel().select(item)
            );
        else
            choice.getSelectionModel().select(0);
        
        initialising = false;
    }
    
    public void setOnFolderViewTypeChanged(Consumer<Map<SortableTreeItem<Machine>, List<String>>> onFolderViewTypeChanged) {
        this.onFolderViewTypeChanged = onFolderViewTypeChanged;
    }
    
    public void setOnCheckAction(BiConsumer<SortableTreeItem<Machine>, Boolean> onCheckAction) {
        this.onCheckAction = onCheckAction;
    }
    
    private List<String> addFolder(
        Map<String, SortableTreeItem<Machine>> index,
        Map<SortableTreeItem<Machine>, List<String>> view,
        String name
    ) {
        return addFolder(index, view, name, true);
    }
    
    private List<String> addFolder(
        Map<String, SortableTreeItem<Machine>> index,
        Map<SortableTreeItem<Machine>, List<String>> view,
        String name,
        boolean addGraphics
    ) {
        SortableTreeItem<Machine> folder = new SortableTreeItem<>(new MachineFolder(name));
        index.put(name, folder);
        List<String> items = new ArrayList<>();
        view.put(folder, items);
        
        if (addGraphics) {
            CheckBox check = new CheckBox(name);
            check.setUserData(folder);
            
            if (
                choice.getSelectionModel().getSelectedItem().toString().equals(Configuration.Manager.getSelectedMachineFolderView()) &&
                Configuration.Manager.getMachineFoldersRemovedFromView().containsKey(name)
            ) {
                check.setSelected(false);
                Platform.runLater(() -> onCheckAction.accept(folder, false));
            } else
                check.setSelected(true);
            
            check.setOnAction(evt -> {
                try {
                    if (check.isSelected())
                        Configuration.Manager.updateMachineFolderAddedIntoView(name);
                    else
                        Configuration.Manager.updateMachineFolderRemovedFromView(name);
                } catch (IOException ex) {
                    Logger.getLogger(MachineFolderViewPaneController.class.getName()).log(
                        Level.SEVERE, "Couldn't save the new status for folder " + name + " in the current folder view", ex
                    );
                }
                onCheckAction.accept(folder, check.isSelected());
            });
            flow.getChildren().add(check);
        }
        
        return items;
    }
    
    private void setSelected(boolean selected) {
        Configuration.Manager.beginUIConfigurationTransaction();
        
        flow.getChildren().stream().map(
            node -> (CheckBox) node
        ).forEach(check -> {
            if (selected != check.isSelected())
                check.fire();
        });
        
        try {
            Configuration.Manager.endUIConfigurationTransaction();
        } catch (IOException ex) {
            Logger.getLogger(MachineFolderViewPaneController.class.getName()).log(
                Level.SEVERE, "Couldn't save the folders removed from the current folder view", ex
            );
        }
    }
    
    @FXML
    private void handleChoiceAction(ActionEvent event) {
        flow.getChildren().clear();
        
        IniPath iniPath = choice.getSelectionModel().getSelectedItem();
        Map<SortableTreeItem<Machine>, List<String>> view = new HashMap<>();
        
        if (iniPath != defaultPath) try {
            Map<String, SortableTreeItem<Machine>> index = new HashMap<>();
            List<String> lines = Files.readAllLines(iniPath.getPath());
            boolean skip = false;
            boolean isMame32Format = false;
            List<String> items = null;
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("[")) switch (line) {
                    case "[FOLDER_SETTINGS]" -> {
                        skip = true;
                        isMame32Format = true;
                    }
                    case "[ROOT_FOLDER]" -> skip = false;
                    default -> {
                        if (skip)
                            skip = false;
                        if (isMame32Format)
                            items = addFolder(index, view, line.substring(1, line.length() - 1));
                    }
                }
                
                if (!skip && !line.isEmpty() && !line.startsWith("[") && !line.startsWith(";"))
                    if (!isMame32Format) {
                        String[] itemFolder = line.split("=");
                        items = view.get(index.get(itemFolder[1]));
                        if (items == null)
                            items = addFolder(index, view, itemFolder[1]);
                        items.add(itemFolder[0]);
                    } else {
                        if (items == null)
                            items = addFolder(index, view, "", false);
                        items.add(line);
                    }
            }
        } catch (IOException ex) {
            Logger.getLogger(MachineFolderViewPaneController.class.getName()).log(Level.SEVERE, "Error while processing .ini file", ex);
        }
        
        try {
            if (!initialising)
                Configuration.Manager.updateSelectedMachineFolderView(iniPath.toString());
        } catch (IOException ex) {
            Logger.getLogger(MachineFolderViewPaneController.class.getName()).log(Level.SEVERE, "Error while saving the newly selected folder view", ex);
        }
        if (onFolderViewTypeChanged != null)
            onFolderViewTypeChanged.accept(view);
        
        selectAllButton.setDisable(flow.getChildren().size() <= 0);
        selectNoneButton.setDisable(flow.getChildren().size() <= 0);
    }

    @FXML
    private void handleSelectAllAction(ActionEvent event) {
        setSelected(true);
    }

    @FXML
    private void handleSelectNoneAction(ActionEvent event) {
        setSelected(false);
    }
}
