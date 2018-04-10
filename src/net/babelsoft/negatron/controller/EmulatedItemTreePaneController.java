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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.Statistics;
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

    public void setViewButtonSelected(boolean selected) {
        viewButton.setSelected(selected);
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
    
    public Statistics setItems(List<T> emulatedItems) {
        treeView.beginResetOperation();
        
        reset();
        Statistics stats = TreeTableDataFiller.fill(treeView, emulatedItems, false);
        if (mustFlatten)
            switchView(false);
        
        treeView.endResetOperation();
        
        return stats;
    }
    
    public void setViewType(Map<SortableTreeItem<T>, List<String>> viewType) {
        treeView.beginTreeWiseOperation();
        List<TreeTableColumn<T, ?>> sortOrder = new ArrayList<>(treeView.getSortOrder());
        treeView.getSortOrder().clear();
        
        SortableTreeItem<T> root = treeView.getSortableRoot();
        root.getInternalChildren().clear();
        viewType.entrySet().forEach(entry -> {
            SortableTreeItem<T> folder = entry.getKey();
            entry.getValue().forEach(itemName -> {
                SortableTreeItem<T> item = treeView.getTreeItem(itemName);
                if (item != null)
                    folder.getInternalChildren().add(item);
            });
            if (folder.getInternalChildren().size() > 0)
                if (!Strings.isEmpty(folder.getValue().getName()))
                    root.getInternalChildren().add(folder);
                else
                    root.getInternalChildren().addAll(folder.getInternalChildren());
        });
        
        treeView.getSortOrder().addAll(sortOrder);
        treeView.endTreeWiseOperation();
    }
    
    private void switchView() {
        switchView(true);
    }
    private void switchView(boolean performTreeWiseEncapsulateOps) {
        if (performTreeWiseEncapsulateOps)
            treeView.beginTreeWiseOperation();
        
        List<TreeTableColumn<T, ?>> sortOrder = new ArrayList<>(treeView.getSortOrder());
        treeView.getSortOrder().clear();
        
        SortableTreeItem<T> root = treeView.getSortableRoot();
        if (mustFlatten)
            root.getInternalChildren().stream().map(
                item -> (SortableTreeItem<T>) item
            ).flatMap(
                item -> item.getInternalChildren().stream()
            ).collect(
                Collectors.toList()
            ).forEach(item -> {
                SortableTreeItem<T> parent = (SortableTreeItem<T>) item.getParent();
                if (parent != null)
                    parent.getInternalChildren().clear();
                root.getInternalChildren().add(item);
            });
        else
            root.getInternalChildren().stream().filter(
                item -> item.getValue().hasParent()
            ).collect(
                Collectors.toList()
            ).forEach(item -> {
                root.getInternalChildren().remove(item);
                treeView.getTreeItem(
                    item.getValue().getParent().getName()
                ).getInternalChildren().add(item);
            });
        
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
