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

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;
import net.babelsoft.negatron.io.Audio;
import net.babelsoft.negatron.io.Audio.Sound;
import net.babelsoft.negatron.model.IconDescription;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.Support;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.model.item.MachineFolder;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.tree.ConfigurableTreeTableCell;
import net.babelsoft.negatron.view.control.tree.DisclosureNode;
import net.babelsoft.negatron.view.control.tree.IconDescriptionTreeTableCell;
import net.babelsoft.negatron.view.control.tree.ImageEnumTreeTableCell;
import net.babelsoft.negatron.view.control.tree.LabelTreeTableCell;
import net.babelsoft.negatron.view.control.tree.NegatronTreeTableCell;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;

/**
 *
 * @author capan
 */
public class EmulatedItemTreeView<T extends EmulatedItem<T>> extends NegatronTreeView<T> {
    
    private final PseudoClass disabledClass = PseudoClass.getPseudoClass("disabled");
    private final Timeline timer;
    
    private Map<Character, Collection<TreeItem<T>>> shortcutMap;
    private Map<String, SortableTreeItem<T>> map;
    private String shortcut;
    private T selectedItem;
    
    private Delegate onAllowAction;
    private Delegate onForbidAction;
    private Delegate onSpaceKeyPressed;
    private Node onEscapeNode;
    
    private Image configurationIcon;
    
    public EmulatedItemTreeView() {
        super();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), actionEvent -> shortcut = ""));
        shortcut = "";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setRowFactory(tree -> {
            TreeTableRow<T> cell = new TreeTableRow<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        pseudoClassStateChanged(disabledClass, false);
                    } else if (item.isNotCompatible())
                        pseudoClassStateChanged(disabledClass, true);
                    else
                        pseudoClassStateChanged(disabledClass, false);
                }
            };

            // Add disclosure
            final StackPane disclosureNode = new DisclosureNode(cell);
            cell.setDisclosureNode(disclosureNode);
            
            return cell;
        });

        getColumns().stream().forEach(col -> { switch (col.getId()) {
            case "iconDescription":
                @SuppressWarnings("unchecked")
                TreeTableColumn<T, IconDescription> col1 = (TreeTableColumn<T, IconDescription>) col;
                setColumnFactory(col1, IconDescriptionTreeTableCell<T>::new, cell -> cell.getValue().getValue().iconDescriptionProperty());
                
                col1.setComparator(Comparator.<IconDescription, String>comparing(
                    iconDescription -> iconDescription.getDescription(),
                    (desc1, desc2) -> desc1.compareToIgnoreCase(desc2)
                ));
                break;
            case "configurable":
                @SuppressWarnings("unchecked")
                TreeTableColumn<T, Boolean> col2 = (TreeTableColumn<T, Boolean>) col;
                setColumnFactory(col2, ConfigurableTreeTableCell<T>::new, cell -> new ReadOnlyBooleanWrapper(cell.getValue().getValue().isConfigurable()));
                break;
            case "support":
                @SuppressWarnings("unchecked")
                TreeTableColumn<T, Support> col3 = (TreeTableColumn<T, Support>) col;
                setColumnFactory(col3, ImageEnumTreeTableCell<T, Support>::new, cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue().getSupport()));
                break;
            case "status":
                @SuppressWarnings("unchecked")
                TreeTableColumn<T, Status> col4 = (TreeTableColumn<T, Status>) col;
                setColumnFactory(col4, ImageEnumTreeTableCell<T, Status>::new, cell -> cell.getValue().getValue().statusProperty());
                break;
            default:
                @SuppressWarnings("unchecked")
                TreeTableColumn<T, String> col5 = (TreeTableColumn<T, String>) col;
                Callback<CellDataFeatures<T, String>, ObservableValue<String>> valueFactory;
                valueFactory = switch (col.getId()) {
                    case "name" -> cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().getName());
                    case "group" -> cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().getGroup());
                    case "year" -> cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().getYear());
                    default -> cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().getCompany());
                };
                setColumnFactory(col5, LabelTreeTableCell<T>::new, valueFactory);
                break;
        }});

        setSortPolicy(tree -> {
            TreeSortMode sortMode = getSortMode();
            if (sortMode == null)
                return false;
            
            SortableTreeItem<T> rootItem = getSortableRoot();
            rootItem.setComparator(tree.getComparator());
            
            return true;
        });
        
        getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
            if (newValue != null) {
                selectedItem = newValue.getValue();
                
                if (selectedItem.isNotCompatible()) {
                    if (onForbidAction != null)
                        onForbidAction.fire();
                } else {
                    if (onAllowAction != null)
                        onAllowAction.fire();
                }
            } else
                selectedItem = null;
        });
        setOnKeyTyped(event -> handleKeyTyped(event));
    }
    
    private <S> void setColumnFactory(
        TreeTableColumn<T, S> col,
        Supplier<? extends NegatronTreeTableCell<T, S>> builder,
        Callback<CellDataFeatures<T, S>, ObservableValue<S>> valueFactory
    ) {
        col.setCellValueFactory(valueFactory);
        col.setCellFactory(column -> {
            NegatronTreeTableCell<T, S> cell = builder.get();
            cell.setOnMouseClicked(evt -> handleMouseClicked(evt));
            return cell;
        });
    };
    
    public void reset() {
        getSelectionModel().clearSelection();
        
        SortableTreeItem<T> root = getSortableRoot();
        root.getInternalChildren().clear();
        
        shortcutMap = null;
        map = null;
    }
    
    public SortableTreeItem<T> getSortableRoot() {
        SortableTreeItem<T> root = (SortableTreeItem<T>) getRoot();
        if (root == null) {
            root = new SortableTreeItem<>();
            root.setExpanded(true);
            setRoot(root);
        }
        return root;
    }
    
    public void setShortcutMap(Map<Character, Collection<TreeItem<T>>> shortcutMap) {
        this.shortcutMap = shortcutMap;
    }
    
    public void setMap(Map<String, SortableTreeItem<T>> map) {
        this.map = map;
    }
    
    public Map<String, SortableTreeItem<T>> getMap() {
        return map;
    }
    
    public SortableTreeItem<T> getTreeItem(String name) {
        return map.get(name);
    }
    
    public Image getConfigurationIcon() {
        return configurationIcon;
    }

    void setConfigurationIcon(Image icon) {
        configurationIcon = icon;
    }

    public void setHeaders(Map<String, String> headers) {
        headers.entrySet().forEach(entry -> getColumns().filtered(
            column -> column.getId().equals(entry.getKey())
        ).stream().forEach(
            column -> column.setText(entry.getValue())
        ));
    }
    
    public void setOnAllowAction(Delegate delegate) {
        onAllowAction = delegate;
    }
    
    public void setOnForbidAction(Delegate delegate) {
        onForbidAction = delegate;
    }
    
    public void setOnSpaceKeyPressed(Delegate delegate) {
        onSpaceKeyPressed = delegate;
    }
    
    public void setOnEscapeNode(Node node) {
        onEscapeNode = node;
    }
    
    public void select(T item) {
        if (item != null) {
            // When a part of the item list has been filtered out and users select
            // a filtered out item, the list may graphically keep the old selection
            // although it has programmatically switched to the new selection.
            // If the user selects back the old selection, no selection update events
            // may be triggered as the graphical status and the programmatical status
            // are clashing with each other.
            // To avoid those weird situations, programmatically clean the status
            // before selecting the new item
            getSelectionModel().clearSelection();
            
            // Selection isn't always successfull on hidden items,
            // so ensure that the item's visible
            SortableTreeItem<T> treeItem = map.get(item.getName());
            if (treeItem.getParent() != null && !treeItem.getParent().isExpanded())
                treeItem.getParent().setExpanded(true);
            
            getSelectionModel().select(treeItem);
            scrollTo(getSelectionModel().getSelectedIndex());
        } else
            getSelectionModel().clearSelection();
    }
    
    @Override
    public void sort() {
        beginTreeWiseOperation();
        super.sort();
        endTreeWiseOperation();
    }
    
    @Override
    protected void performAllExpanded(List<TreeItem<T>> children, boolean value) {
        children.stream().filter(
            child -> child != null && !child.isLeaf()
        ).forEach(child -> child.setExpanded(value));
    }
    
    public void beginResetOperation() {
        beginTreeWiseOperation();
    }
    
    public void endResetOperation() {
        if (selectedTreeItem != null) {
            String ref = selectedTreeItem.getValue().getName();
            TreeTableViewSelectionModel<T> selection = getSelectionModel();
            // We have no option but to iterate through the model and select the
            // first occurrence of the given object. Once we find the first one, we
            // don't proceed to select any others.
            for (int i = 0, max = getExpandedItemCount(); i < max; i++) {
                TreeItem<T> rowObj = selection.getModelItem(i);

                if (rowObj.getValue().getName().equals(ref)) {
                    selectedTreeItem = rowObj;
                    endTreeWiseOperation();
                    return;
                } else if (!rowObj.getChildren().isEmpty())
                    for (TreeItem<T> child : rowObj.getChildren())
                        if (child.getValue().getName().equals(ref)) {
                            rowObj.setExpanded(true);
                            selectedTreeItem = child;
                            endTreeWiseOperation();
                            return;
                        }
            }
        }
    }
    
    private void handleKeyTyped(KeyEvent event) {
        if (!event.getCharacter().matches("[a-z0-9]"))
            return;
        
        timer.stop();

        shortcut += event.getCharacter().toLowerCase();
        char index = shortcut.charAt(0);
        Collection<TreeItem<T>> items = shortcutMap.get(index);
        
        if (items != null)
            items.stream().filter(
                item -> item.getParent() != null && item.getValue().getName().matches("^" + shortcut + ".*")
            ).findFirst().ifPresent(item -> {
                TreeItem<T> parent = item.getParent();
                if (parent != getRoot() && !parent.isExpanded())
                    parent.setExpanded(true);

                int idx = getRow(item);
                scrollTo(idx);
                getSelectionModel().select(idx);
        });

        timer.playFromStart();
    }
    
    @Override
    protected void handleAction() {
        if (selectedItem != null)
            if (selectedItem.isNotCompatible()) {
                if (selectedItem instanceof MachineFolder) {
                    TreeItem<T> item = getSelectionModel().getSelectedItem();
                    item.setExpanded(!item.isExpanded());
                } else {
                    Alert alert = new Alert(
                        AlertType.ERROR,
                        Language.Manager.getString("invalidSelection.error")
                    );
                    alert.initOwner(getScene().getWindow());
                    alert.show();
                    Audio.play(Sound.ERROR);
                }
            } else
                super.handleAction();
    }
    
    @Override
    public void handleKeyPressed(KeyEvent event) {
        super.handleKeyPressed(event);
        
        if (!event.isConsumed())
            if (event.getCode() == KeyCode.SPACE && onSpaceKeyPressed != null)
                onSpaceKeyPressed.fire();
            else if (event.getCode() == KeyCode.ESCAPE && onEscapeNode != null && onEscapeNode.getOnKeyPressed() != null)
                onEscapeNode.getOnKeyPressed().handle(event);
    }
}
