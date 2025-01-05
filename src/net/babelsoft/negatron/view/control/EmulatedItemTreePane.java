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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.controller.EmulatedItemTreePaneController;
import net.babelsoft.negatron.controller.FilterPaneController;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Editable;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;

/**
 *
 * @author capan
 */
public class EmulatedItemTreePane<T extends EmulatedItem<T>> extends VBox {
    
    private EmulatedItemTreePaneController<T> controller;
    private EmulatedItemTreeView<T> tree;
    
    public EmulatedItemTreePane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/EmulatedItemTreePane.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
            tree = controller.getTreeView();
            idProperty().addListener(
                (o, oV, newValue) -> controller.setId(newValue)
            );
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void reset() {
        controller.reset();
    }
    
    public void setFilterPane(FilterPane<T, ? extends FilterPaneController<T>> pane) {
        pane.setTreeView(tree);
        controller.setOnFilter(pane);
    }
    
    public void setOnMoreFilters(Consumer<Boolean> onMoreFilters) {
        controller.setOnMoreFilters(onMoreFilters);
    }
    
    public void setFilterButtonSelected(boolean selected) {
        controller.setFilterButtonSelected(selected);
    }
    
    public Property<String> filterProperty() {
        return controller.filterProperty();
    }

    public void closeFilterPane() {
        if (controller.isFilterButtonSelected())
            controller.fireFilterButton();
    }

    void unbindFilter() {
        controller.unbindFilter();
    }
    
    public void clearSelection() {
        tree.getSelectionModel().clearSelection();
    }
    
    public void setItems(List<T> emulatedItems) {
        controller.setItems(emulatedItems);
    }
    
    public void setOnAction(Delegate delegate) {
        tree.setOnAction(delegate);
    }
    
    public void setOnAllowAction(Delegate delegate) {
        tree.setOnAllowAction(delegate);
    }
    
    public void setOnForbidAction(Delegate delegate) {
        tree.setOnForbidAction(delegate);
    }
    
    public void setOnSpaceKeyPressed(Delegate delegate) {
        tree.setOnSpaceKeyPressed(delegate);
    }
    
    public void setOnEscapeNode(Node node) {
        tree.setOnEscapeNode(node);
    }

    public void setOnTreeWiseOperation(Consumer<Boolean> delegate) {
        tree.setOnTreeWiseOperation(delegate);
    }
    
    public void SetOnceOnTreeWiseOperationEnded(Delegate delegate) {
        tree.setOnceOnTreeWiseOperationEnded(delegate);
    }
    
    public T getCurrentItem() {
        TreeItem<T> treeItem = tree.getSelectionModel().getSelectedItem();
        if (treeItem != null)
            return treeItem.getValue();
        else
            return null;
    }
    
    public void setCurrentItem(T item) {
        tree.select(item);
    }
    
    public ReadOnlyObjectProperty<TreeItem<T>> currentItemProperty() {
        return tree.getSelectionModel().selectedItemProperty();
    }
    
    public void scrollToSelection() {
        tree.scrollTo(tree.getSelectionModel().getSelectedIndex());
    }
    
    public void setOnMoreViews(Consumer<Boolean> onMoreViews) {
        controller.setOnMoreViews(onMoreViews);
    }
    
    public void setMoreViews(boolean enable) {
        controller.setMoreViews(enable);
    }
    
    public boolean isMoreViews() {
        return true;
    }

    public void setViewButtonSelected(boolean selected) {
        controller.setViewButtonSelected(selected);
    }

    public void closeViewPane() {
        if (controller.isViewButtonSelected())
            controller.fireViewButton();
    }
    
    public void setConfigurationIcon(Image icon) {
        tree.setConfigurationIcon(icon);
    }
    
    public Image getConfigurationIcon() { return null; } // useless method, just here to let FXML parser happy
    
    public void setHeaders(Map<String, String> headers) {
        tree.setHeaders(headers);
    }
    
    public Map<String, String> getHeaders() { return null; } // useless method, just here to let FXML parser happy
    
    public void setOnTreeMouseClicked(EventHandler<? super MouseEvent> value) {
        tree.setOnMouseClicked(value);
    }
    
    public ObjectProperty<EventHandler<? super MouseEvent>> onTreeMouseClickedProperty() {
        return tree.onMouseClickedProperty();
    }
    
    public void requestTreeFocus() {
        tree.requestFocus();
    }

    public void setFolderViewType(Map<SortableTreeItem<T>, List<String>> folderViewType) {
        controller.setFolderViewType(folderViewType);
    }
    
    public void setFolderVisible(SortableTreeItem<T> folder, boolean visible) {
        controller.setFolderVisible(folder, visible);
    }

    public void setEditableControl(Editable editable) {
        tree.setEditableControl(editable);
    }
}
