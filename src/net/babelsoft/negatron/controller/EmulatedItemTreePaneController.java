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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;
import net.babelsoft.negatron.view.control.FilterPane;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;
import net.babelsoft.negatron.view.control.tree.TreeItemPredicate;
import net.babelsoft.negatron.view.control.tree.TreeTableDataFiller;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class EmulatedItemTreePaneController<T extends EmulatedItem<T>> extends TreePaneController<EmulatedItemTreeView<T>, T> {
    
    @FXML
    private ToggleGroup displayToggle;
    @FXML
    private ToggleButton treeViewButton;
    @FXML
    private ToggleButton tableViewButton;
    @FXML
    private ToggleButton viewButton;
    @FXML
    private TextField filterField;
    @FXML
    private ToggleButton filterButton;
    @FXML
    private Button expandAllButton;
    @FXML
    private Button collapseAllButton;
    
    private boolean mustFlatten;
    
    private Timeline filterTimeline;
    private Consumer<Boolean> onMoreViews;
    private Consumer<Boolean> onMoreFilters;
    private ChangeListener<? super String> filterChangeListener;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // MESS adornment
        if (Configuration.Manager.isMess()) {
            Tooltip tooltip = treeView.getTooltip();
            tooltip.setText(tooltip.getText().replace("MAME", "MESS"));
        }
        
        super.initialize(url, rb);
        
        filterTimeline = new Timeline(new KeyFrame(Duration.millis(300), event -> {
            treeView.beginTreeWiseOperation();
            SortableTreeItem<T> root = (SortableTreeItem<T>) treeView.getRoot();
            
            if (Strings.isValid(filterField.getText())) {
                String filter = filterField.getText().replace(" ", "").toLowerCase();
                root.setPredicate(TreeItemPredicate.create(
                    emulatedItem -> emulatedItem.getShortcut().contains(filter)
                ));
            } else
                root.setPredicate(null);
            
            treeView.endTreeWiseOperation();
        }));
        filterChangeListener = (o, oV, nV) -> filterTimeline.playFromStart();
        filterField.textProperty().addListener(filterChangeListener);
    }
    
    public void reset() {
        treeView.reset();
    }
    
    public EmulatedItemTreeView<T> getTreeView() {
        return treeView;
    }
    
    public void setMoreViews(boolean enable) {
        if (!enable)
            ((Pane) viewButton.getParent()).getChildren().remove(viewButton);
    }
    
    public void setOnMoreViews(Consumer<Boolean> onMoreViews) {
        this.onMoreViews = onMoreViews;
    }
    
    public boolean isViewButtonSelected() {
        return viewButton.isSelected();
    }

    public void setViewButtonSelected(boolean selected) {
        viewButton.setSelected(selected);
    }

    public void fireViewButton() {
        viewButton.fire();
    }

    public Property<String> filterProperty() {
        return filterField.textProperty();
    }
    
    public void setOnFilter(FilterPane<T, ? extends FilterPaneController<T>> pane) {
        pane.setOnFilter(isDefaults -> {
            if (isDefaults)
                filterButton.setText("+");
            else
                filterButton.setText("±");
        });
    }
    
    public void setOnMoreFilters(Consumer<Boolean> onMoreFilters) {
        this.onMoreFilters = onMoreFilters;
    }
    
    public boolean isFilterButtonSelected() {
        return filterButton.isSelected();
    }

    public void setFilterButtonSelected(boolean selected) {
        filterButton.setSelected(selected);
    }

    public void fireFilterButton() {
        filterButton.fire();
    }

    public void unbindFilter() {
        filterField.textProperty().removeListener(filterChangeListener);
        filterChangeListener = null;
        filterTimeline = null;
    }
    
    public void setItems(List<T> emulatedItems) {
        treeView.beginResetOperation();
        
        reset();
        TreeTableDataFiller.fill(treeView, emulatedItems, false);
        if (mustFlatten)
            switchView(false);
        
        treeView.endResetOperation();
    }
    
    public void setFolderViewType(Map<SortableTreeItem<T>, List<String>> folderViewType) {
        treeView.beginTreeWiseOperation();
        List<TreeTableColumn<T, ?>> sortOrder = new ArrayList<>(treeView.getSortOrder());
        treeView.getSortOrder().clear();
        
        treeView.getMap().values().forEach(item -> {
            if (item.getParent() != null)
                ((SortableTreeItem<T>) item.getParent()).getInternalChildren().clear();
            item.getInternalChildren().clear();
        });
        
        SortableTreeItem<T> root = treeView.getSortableRoot();
        root.getInternalChildren().clear();
        
        if (folderViewType.size() > 0)
            folderViewType.entrySet().forEach(entry -> {
                SortableTreeItem<T> folder = entry.getKey();
                boolean canAddToFolder = !Strings.isEmpty(folder.getValue().getName());
                
                entry.getValue().forEach(itemName -> {
                    SortableTreeItem<T> item = treeView.getTreeItem(itemName);
                    if (item != null)
                        if (canAddToFolder)
                            folder.getInternalChildren().add(item);
                        else
                            root.getInternalChildren().add(item);
                });
                
                if (folder.getInternalChildren().size() > 0 && canAddToFolder)
                    root.getInternalChildren().add(folder);
            });
        else
            treeView.getMap().values().forEach(item -> {
                if (mustFlatten || !item.getValue().hasParent())
                    root.getInternalChildren().add(item);
                else
                    treeView.getTreeItem(
                        item.getValue().getParent().getName()
                    ).getInternalChildren().add(item);
            });
        
        treeView.getSortOrder().addAll(sortOrder);
        treeView.endTreeWiseOperation();
    }
    
    public void setFolderVisible(SortableTreeItem<T> folder, boolean visible) {
        if (visible)
            treeView.getSortableRoot().getInternalChildren().add(folder);
        else
            treeView.getSortableRoot().getInternalChildren().remove(folder);
    }
    
    private void switchView() {
        switchView(true);
    }
    Map<String, SortableTreeItem<T>> parentMap = new HashMap<>();
    private void switchView(boolean performTreeWiseEncapsulateOps) {
        if (performTreeWiseEncapsulateOps)
            treeView.beginTreeWiseOperation();
        
        List<TreeTableColumn<T, ?>> sortOrder = new ArrayList<>(treeView.getSortOrder());
        treeView.getSortOrder().clear();
        
        ObservableList<TreeItem<T>> rootChildren = treeView.getSortableRoot().getInternalChildren();
        if (mustFlatten) {
            rootChildren.stream().map(
                item -> (SortableTreeItem<T>) item
            ).flatMap(
                item -> item.getInternalChildren().stream()
            ).collect(
                Collectors.toList()
            ).forEach(item -> {
                SortableTreeItem<T> parent = (SortableTreeItem<T>) item.getParent();
                if (parent != null && parent.getValue() != null) {
                    parent.getInternalChildren().clear();
                    
                    if (parent.getValue().isNotCompatible()) {
                        rootChildren.remove(parent);
                        // Tree item caching for subsequent view switchings.
                        // Tree items must be recycled as tree views completely ignore subsequent creations of tree items on the same value for some reasons.
                        // A nice side-effect of this caching is that it improves graphical performances.
                        parentMap.put(parent.getValue().getName(), parent);
                    }
                }
                rootChildren.add(item);
            });
        } else {
            List<TreeItem<T>> list = rootChildren.stream().filter(
                item -> item.getValue().hasParent()
            ).collect(
                Collectors.toList()
            );
            
            boolean dirty = false;
            for (TreeItem<T> item : list) {
                rootChildren.remove(item);
                String parentName = item.getValue().getParent().getName();
                SortableTreeItem<T> parentTreeItem = (SortableTreeItem<T>) treeView.getTreeItem(parentName);
                if (parentTreeItem == null)
                    parentTreeItem = parentMap.get(parentName);
                if (parentTreeItem == null) {
                    T parent = item.getValue().getParent();
                    parent.setNotCompatible();
                    parentTreeItem = new SortableTreeItem<>(parent);
                    parentMap.put(parentName, parentTreeItem);
                    rootChildren.add(parentTreeItem);
                } else if (!rootChildren.contains(parentTreeItem)) {
                    rootChildren.add(parentTreeItem);
                    dirty = true; // flag to indicate that previously removed tree items have been added back to the tree view
                }
                parentTreeItem.getInternalChildren().add(item);
            }
            
            if (dirty) {
                // A tree view easily handles:
                // - New tree items added to its children collection,
                // - Tree items that were added then removed from this collection.
                // But for some reasons, it badly handles:
                // - Tree items that are removed and then added back to it,
                // - New tree items that have the same value as a removed tree item.
                // Adding such items to its children collection should trigger the wires to update its graphical state but it does not.
                // So, the current tree view is forcefully reset to a clean state
                list = new ArrayList<>(rootChildren);
                rootChildren.clear();
                rootChildren.addAll(list);
            }
        }
        
        treeView.getSortOrder().addAll(sortOrder);
        
        if (performTreeWiseEncapsulateOps)
            treeView.endTreeWiseOperation();
    }

    @Override
    protected void loadLayout() {
        if (Configuration.Manager.getTreeTableFlattenConfiguration(id)) {
            displayToggle.selectToggle(tableViewButton);
            setDisable(true);
        } else
            displayToggle.selectToggle(treeViewButton);
        super.loadLayout();
    }
    
    private void setDisable(boolean value) {
        expandAllButton.setDisable(value);
        collapseAllButton.setDisable(value);
        mustFlatten = value;
    }

    @FXML
    private void handleTreeViewSwitchAction(ActionEvent event) {
        if (!treeViewButton.isSelected()) {
            tableViewButton.setSelected(true);
            handleTableViewSwitchAction(event);
            return;
        }
        
        setDisable(false);
        try {
            Configuration.Manager.updateTreeTableFlattenConfiguration(id, mustFlatten);
        } catch (IOException ex) {
            Logger.getLogger(EmulatedItemTreePaneController.class.getName()).log(Level.SEVERE, "Tree table flatten configuration failed", ex);
        }
        switchView();
    }

    @FXML
    private void handleTableViewSwitchAction(ActionEvent event) {
        if (!tableViewButton.isSelected()) {
            treeViewButton.setSelected(true);
            handleTreeViewSwitchAction(event);
            return;
        }
        
        setDisable(true);
        try {
            Configuration.Manager.updateTreeTableFlattenConfiguration(id, mustFlatten);
        } catch (IOException ex) {
            Logger.getLogger(EmulatedItemTreePaneController.class.getName()).log(Level.SEVERE, "Tree table flatten configuration failed", ex);
        }
        switchView();
    }
    
    @FXML
    private void handleMoreViewsAction(ActionEvent event) {
        onMoreViews.accept(viewButton.isSelected());
    }
    
    @FXML
    private void handleMoreFiltersAction(ActionEvent event) {
        onMoreFilters.accept(filterButton.isSelected());
    }

    @FXML
    private void handleExpandAllAction(ActionEvent event) {
        treeView.expandAll();
    }

    @FXML
    private void handleCollapseAllAction(ActionEvent event) {
        treeView.collapseAll();
    }
}
