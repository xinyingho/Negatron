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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.io.configuration.PathCharset;
import net.babelsoft.negatron.io.configuration.PathPrimary;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.Infotip;

/**
 *
 * @author capan
 */
public class MultiPathField extends Field {
    
    private static final List<MultiPathField> list = new ArrayList<>();
    
    public static List<MultiPathField> getList() {
        return list;
    }

    private final Label label;
    private final GridPane internalGrid;
    private final ToggleGroup machineToggleGroup;
    private final ToggleGroup softwareToggleGroup;
    private final String promptText;
    private final Property property;
    private final String[] fileFilters;
    private int internalCount;
    
    private final List<TextField> pathFields;
    private final List<ChoiceBox<String>> choices;
    private final List<RadioButton> machineRadios;
    private final List<RadioButton> softwareRadios;
    
    public MultiPathField(GridPane grid, int row, Property property, String text, String promptText, String... fileFilters) {
        list.add(this);
        
        this.promptText = promptText;
        this.property = property;
        this.fileFilters = fileFilters;
        
        pathFields = new ArrayList<>();
        internalCount = 0;
        if (property.getDomain() == Domain.EXTRAS_INFORMATION)
            choices = new ArrayList<>();
        else
            choices = null;
        switch (property.getDomain()) {
            case EXTRAS_MACHINE_ONLY:
            case MULTIMEDIA_MACHINE_ONLY:
                machineToggleGroup = new ToggleGroup();
                softwareToggleGroup = null;
                machineRadios = new ArrayList<>();
                softwareRadios = null;
                break;
            case EXTRAS_SOFTWARE_ONLY:
                machineToggleGroup = null;
                softwareToggleGroup = new ToggleGroup();
                machineRadios = null;
                softwareRadios = new ArrayList<>();
                break;
            case EXTRAS_MACHINE_SOFTWARE:
            case MULTIMEDIA_MACHINE_SOFTWARE:
                machineToggleGroup = new ToggleGroup();
                softwareToggleGroup = new ToggleGroup();
                machineRadios = new ArrayList<>();
                softwareRadios = new ArrayList<>();
                break;
            default:
                machineToggleGroup = null;
                softwareToggleGroup = null;
                machineRadios = null;
                softwareRadios = null;
                break;
        }
        
        if (
            property.getDomain() == Domain.EXTRAS_MACHINE_ONLY ||
            property.getDomain() == Domain.EXTRAS_MACHINE_SOFTWARE ||
            property.getDomain() == Domain.MULTIMEDIA_MACHINE_ONLY ||
            property.getDomain() == Domain.MULTIMEDIA_MACHINE_SOFTWARE
        ) machineToggleGroup.selectedToggleProperty().addListener((o, oV, newValue) -> {
            int index = machineRadios.indexOf(newValue);
            updateMachinePrimaryPath(property, pathFields.get(index).getText());
        });
        if (
            property.getDomain() == Domain.EXTRAS_SOFTWARE_ONLY ||
            property.getDomain() == Domain.EXTRAS_MACHINE_SOFTWARE ||
            property.getDomain() == Domain.MULTIMEDIA_MACHINE_SOFTWARE
        ) softwareToggleGroup.selectedToggleProperty().addListener((o, oV, newValue) -> {
            int index = softwareRadios.indexOf(newValue);
            updateSoftwarePrimaryPath(property, pathFields.get(index).getText());
        });
        
        // add dummy constraints for current row
        RowConstraints constraints = new RowConstraints();
        grid.getRowConstraints().add(constraints);
        
        label = new Label(text);
        label.setWrapText(true);
        grid.add(label, 0, row);
        
        internalGrid = new GridPane();
        internalGrid.setHgap(5.0);
        internalGrid.setVgap(2.0);
        internalGrid.getStyleClass().add("multi-path-field");
        grid.add(internalGrid, 1, row);
        GridPane.setHgrow(internalGrid, Priority.SOMETIMES);
        GridPane.setColumnSpan(internalGrid, 2);
        
        Configuration.Manager.beginMasterConfigTransaction();
        
        if (property.getDomain() == Domain.EXTRAS_INFORMATION) {
            List<PathCharset> files = Configuration.Manager.getFilePaths(property);
            if (files.size() > 0) {
                for (int i = 0; i < files.size(); ++i) {
                    Button button = newField(
                        fileFilters,
                        files.get(i).getPath().toString(),
                        files.get(i).getCharSet()
                    );
                    if (i == 0) {
                        button.setText("+");
                        button.setOnAction(event -> newField(fileFilters));
                    }
                }
            } else {
                Button addButton = newField(fileFilters);
                addButton.setText("+");
                addButton.setOnAction(event -> newField(fileFilters));
            }
        } else {
            List<String> folders = Configuration.Manager.getFolderPaths(property);
            if (folders.size() > 0) {
                for (int i = 0; i < folders.size(); ++i) {
                    Button button = newField(fileFilters, folders.get(i));
                    if (i == 0) {
                        button.setText("+");
                        button.setOnAction(event -> newField(fileFilters));
                    }
                    if (folders.get(i).equals(Configuration.Manager.getPrimaryMachineFolder(property)))
                        machineRadios.get(i).setSelected(true);
                    if (folders.get(i).equals(Configuration.Manager.getPrimarySoftwareFolder(property)))
                        softwareRadios.get(i).setSelected(true);
                }
            } else {
                Button addButton = newField(fileFilters);
                addButton.setText("+");
                addButton.setOnAction(event -> newField(fileFilters));
                
                if (property.hasMachinePrimaryPath())
                    machineRadios.get(0).setSelected(true);
                if (property.hasSoftwarePrimaryPath())
                    softwareRadios.get(0).setSelected(true);
            }
        }
        
        Configuration.Manager.rollbackMasterConfigTransaction(); // initialisation sequence, so no need to write anything
    }
    
    private Button newField(String[] fileFilters, String... initialisingValues) {
        int column = 0;
        
        TextField pathField = new TextField();
        pathField.setPromptText(promptText);
        pathField.setTooltip(new Infotip(promptText));
        internalGrid.add(pathField, column++, internalCount);
        GridPane.setHgrow(pathField, Priority.SOMETIMES);
        pathFields.add(pathField);
        if (initialisingValues.length > 0 && initialisingValues[0] != null)
            pathField.setText(initialisingValues[0]);
        pathField.textProperty().addListener((o, oV, newValue) -> {
            int index = pathFields.indexOf(pathField);
            if (index == -1)
                return;
            
            if (property.getDomain() != Domain.EXTRAS_INFORMATION) {
                updateFolderPath(property, index, newValue);
                if (property.hasMachinePrimaryPath() && machineRadios.get(index).isSelected())
                    updateMachinePrimaryPath(property, newValue);
                if (property.hasSoftwarePrimaryPath() && softwareRadios.get(index).isSelected())
                    updateSoftwarePrimaryPath(property, newValue);
            } else
                updateFilePath(property, index, newValue, choices.get(index).getValue());
        });
        
        @SuppressWarnings("unchecked")
        ChoiceBox<String> choice;
        Button fileButton;
        if (property.getDomain() == Domain.EXTRAS_INFORMATION) {
            choice = new ChoiceBox<>(FXCollections.observableArrayList(
                StandardCharsets.UTF_8.name(),
                StandardCharsets.UTF_16.name(),
                StandardCharsets.ISO_8859_1.name(),
                StandardCharsets.US_ASCII.name()
            ));
            internalGrid.add(choice, column++, internalCount);
            choices.add(choice);
            if (initialisingValues.length > 1 && initialisingValues[1] != null)
                choice.getSelectionModel().select(initialisingValues[1]);
            else
                choice.getSelectionModel().selectFirst();
            choice.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
                int index = choices.indexOf(choice);
                if (index != -1)
                    updateFilePath(property, index, pathFields.get(index).getText(), newValue);
            });
            
            fileButton = null;
        } else if (property.getDomain() != Domain.MAME_FOLDER_ONLY && fileFilters.length > 0) {
            choice = null;
            
            fileButton = new Button(Language.Manager.getString("file..."));
            internalGrid.add(fileButton, column++, internalCount);
            fileButton.setOnAction(event -> {
                File f = handleOnFileAction(pathField, fileFilters);
                if (f != null) {
                    int i = f.getAbsolutePath().lastIndexOf(".");
                    pathField.setText(
                        f.getAbsolutePath().substring(0, i)
                    );
                }
            });
        } else {
            choice = null;
            fileButton = null;
        }
        
        String browseLabel = Language.Manager.getString("folder...");
        if (property.getDomain() == Domain.MAME_FOLDER_ONLY || property.getDomain() == Domain.EXTRAS_INFORMATION || fileFilters.length == 0)
            browseLabel = Language.Manager.getString("browse...");
            
        Button browseButton = new Button(browseLabel);
        internalGrid.add(browseButton, column++, internalCount);
        browseButton.setOnAction(event -> {
            File f = handleOnBrowseAction(pathField, fileFilters);
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
        
        int subtractColumn = column++;
        
        RadioButton machineRadio;
        RadioButton softwareRadio;
        switch (property.getDomain()) {
            case EXTRAS_MACHINE_SOFTWARE:
            case MULTIMEDIA_MACHINE_SOFTWARE:
                machineRadio = addRadio(machineToggleGroup, column++);
                softwareRadio = addRadio(softwareToggleGroup, column);
                machineRadios.add(machineRadio);
                softwareRadios.add(softwareRadio);
                break;
            case EXTRAS_MACHINE_ONLY:
            case MULTIMEDIA_MACHINE_ONLY:
                machineRadio = addRadio(machineToggleGroup, column);
                machineRadios.add(machineRadio);
                softwareRadio = null;
                break;
            case EXTRAS_SOFTWARE_ONLY:
                machineRadio = null;
                softwareRadio = addRadio(softwareToggleGroup, column);
                softwareRadios.add(softwareRadio);
                break;
            default:
                machineRadio = null;
                softwareRadio = null;
                break;
        }
        
        Button subtractButton = new Button();
        internalGrid.add(subtractButton, subtractColumn, internalCount);
        subtractButton.setText("-");
        subtractButton.setOnAction(event -> handleOnSubtractButton(
            pathField, fileButton, browseButton, subtractButton,
            choice, machineRadio, softwareRadio
        ));
        
        ++internalCount;
        return subtractButton;
    }
    
    private RadioButton addRadio(ToggleGroup toggleGroup, int column) {
        RadioButton radio = new RadioButton();
        
        radio.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        radio.setToggleGroup(toggleGroup);
        internalGrid.add(radio, column, internalCount);
        
        return radio;
    }

    public List<TextField> addDefaultPaths(String rootFolder) {
        List<TextField> newFields = new ArrayList<>();
        
        if (property.getDomain() == Domain.EXTRAS_INFORMATION) {
            List<PathCharset> files = Configuration.Manager.getDefaultFilePaths(property, rootFolder);
            if (files != null && files.size() > 0) {
                BiConsumer<Integer, Integer> handleFile = (i, j) -> {
                    pathFields.get(i).setText(files.get(j).getPathString());
                    choices.get(i).getSelectionModel().select(files.get(j).getCharSet());
                    newFields.add(pathFields.get(i));
                };
                
                int i = pathFields.size(), j = 0;
                if (i == 1 && pathFields.get(0).getText().isEmpty())
                    handleFile.accept(0, j++);
                    
                for (; j < files.size(); ++i, ++j) {
                    if (pathFields.size() <= i)
                        newField(fileFilters);
                    handleFile.accept(i, j);
                }
            }
        } else {
            List<PathPrimary> folders = Configuration.Manager.getDefaultFolderPaths(property, rootFolder);
            if (folders != null && folders.size() > 0) {
                BiConsumer<Integer, Integer> handleFolder = (i, j) -> {
                    pathFields.get(i).setText(folders.get(j).getPath());
                    
                    if (folders.get(j).isMachinePrimaryPath())
                        machineRadios.get(i).setSelected(true);
                    if (folders.get(j).isSoftwarePrimaryPath())
                        softwareRadios.get(i).setSelected(true);
                    
                    newFields.add(pathFields.get(i));
                };
                
                int i = pathFields.size(), j = 0;
                if (i == 1 && pathFields.get(0).getText().isEmpty())
                    handleFolder.accept(0, j++);
                
                for (; j < folders.size(); ++i, ++j) {
                    if (pathFields.size() <= i)
                        newField(fileFilters);
                    handleFolder.accept(i, j);
                }
            }
        }
        
        return newFields;
    }
    
    boolean isMamePath() {
        return property.isMamePath();
    }
    
    boolean isExtrasPath() {
        return property.isExtrasPath();
    }
    
    boolean isMultimediaPath() {
        return property.isMultimediaPath();
    }
    
    public List<TextField> getPathFields() {
        return pathFields;
    }
    
    public List<Path> getPaths() {
        return pathFields.stream().map(
                field -> field.getText()
        ).map(
                text -> Path.of(text)
        ).collect(
                Collectors.toList()
        );
    }
    
    public Label getLabel() {
        return label;
    }
    
    public Node getNode() {
        return internalGrid;
    }
    
    private File getInitialDirectory(TextField pathField) {
        String initialPath = pathField.getText();
        if (Strings.isEmpty(initialPath))
            initialPath = ".";
        File initialFile = new File(initialPath);
        if (initialFile.getParent() != null)
            initialFile = initialFile.getParentFile();
        return initialFile;
    }
    
    private File handleOnFileAction(TextField pathField, String[] fileFilters) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(getInitialDirectory(pathField));
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(fileFilters[0], fileFilters[1])
        );
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(Language.Manager.getString("allFiles"), "*")
        );
        File f;
        try {
            f = fc.showOpenDialog(internalGrid.getScene().getWindow());
        } catch (IllegalArgumentException ex) {
            fc.setInitialDirectory(new File("."));
            f = fc.showOpenDialog(internalGrid.getScene().getWindow());
        }

        return f;
    }
    
    private File handleOnBrowseAction(TextField pathField, String[] fileFilters) {
        if (property.getDomain() != Domain.EXTRAS_INFORMATION) {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setInitialDirectory(getInitialDirectory(pathField));
            File f;
            try {
                f = dc.showDialog(internalGrid.getScene().getWindow());
            } catch (IllegalArgumentException ex) {
                dc.setInitialDirectory(new File("."));
                f = dc.showDialog(internalGrid.getScene().getWindow());
            }

            return f;
        } else
            return handleOnFileAction(pathField, fileFilters);
    }
    
    private void handleOnSubtractButton(
        TextField pathField, Button fileButton, Button browseButton, Button subtractButton,
        ChoiceBox<String> choice, RadioButton machineRadio, RadioButton softwareRadio
    ) {
        int index = pathFields.indexOf(pathField);

        ObservableList<Node> nodes = internalGrid.getChildren();
        nodes.remove(pathField);
        nodes.remove(fileButton);
        nodes.remove(browseButton);
        nodes.remove(subtractButton);
        pathFields.remove(pathField);
        if (choice != null) {
            nodes.remove(choice);
            choices.remove(choice);
        }
        if (machineRadio != null) {
            nodes.remove(machineRadio);
            machineRadios.remove(machineRadio);
            if (machineRadio.isSelected())
                machineRadios.get(0).setSelected(true);
        }
        if (softwareRadio != null) {
            nodes.remove(softwareRadio);
            softwareRadios.remove(softwareRadio);
            if (softwareRadio.isSelected())
                softwareRadios.get(0).setSelected(true);
        }
        
        removePath(property, index);
    }
}
