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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.FavouriteConfiguration;
import net.babelsoft.negatron.io.configuration.FavouriteTree;
import net.babelsoft.negatron.model.IconDescription;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.Folder;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.Separator;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.FavouriteTreeView;
import net.babelsoft.negatron.view.control.FavouriteTreeView.Position;
import net.babelsoft.negatron.view.control.tree.CopyPastableTreeItem;
import net.babelsoft.negatron.view.control.tree.CopyPastableTreeItem.CutCopyState;
import net.babelsoft.negatron.view.control.tree.DateTimeTreeTableCell;
import net.babelsoft.negatron.view.control.tree.FavouriteTreeTableCell;
import static net.babelsoft.negatron.view.control.tree.FavouriteTreeTableCell.EDITABLE_CLASS;
import net.babelsoft.negatron.view.control.tree.IconNameTreeTableCell;
import net.babelsoft.negatron.view.control.tree.InteractiveTreeTableCell;
import net.babelsoft.negatron.view.control.tree.MachineConfigurationTreeTableCell;
import net.babelsoft.negatron.view.control.tree.MachineTreeTableCell;
import net.babelsoft.negatron.view.control.tree.SoftwareConfigurationTreeTableCell;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;
import net.babelsoft.negatron.view.control.tree.TreeItemPredicate;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class FavouriteTreePaneController extends TreePaneController<FavouriteTreeView, Favourite> implements ConfigurationChangeListener {

    @FXML
    private SplitPane favouriteSplitPane;
    @FXML
    private AnchorPane bottomPlaceholder;
    
    @FXML
    private Button newFolderButton;
    @FXML
    private Button newFavouriteButton;
    @FXML
    private Button newSeparatorButton;
    
    @FXML
    private TextField filterField;
    @FXML
    private Button expandAllButton;
    @FXML
    private Button collapseAllButton;
    
    @FXML
    private Button cutButton;
    @FXML
    private Button copyButton;
    @FXML
    private Button pasteButton;
    
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    
    @FXML
    private TreeTableColumn<Favourite, IconDescription> iconName;
    @FXML
    private TreeTableColumn<Favourite, Machine> machine;
    @FXML
    private TreeTableColumn<Favourite, SoftwareConfiguration> softwareConfiguration;
    @FXML
    private TreeTableColumn<Favourite, MachineConfiguration> machineConfiguration;
    @FXML
    private TreeTableColumn<Favourite, LocalDateTime> dateCreated;
    @FXML
    private TreeTableColumn<Favourite, LocalDateTime> dateModified;
    
    private Image rootIcon;
    private Image folderIcon;
    private Image anonymousIcon;
    
    private List<Folder> folderList;
    private TreeTableViewSelectionModel<Favourite> selection;
    
    private boolean isShiftPressed;
    private boolean isDragDone;
    
    private Consumer<Favourite> onCommitted;
    private FavouriteTreeTableCell hoveredCell;
    private FavouriteTreeTableCell editingCell;
    private EditController editController;
    private boolean isInserting;
    private boolean committing;
    private boolean isHidingMachineList;
    
    private Divider listDivider;
    private Node listDividerGrabber;
    private Timeline listTimeline;
    
    private FavouriteConfiguration xmlConf;
    private FavouriteTree initialTree;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        
        treeView.setColumnFactory(iconName, () -> new IconNameTreeTableCell(this));
        treeView.setColumnFactory(machine, () -> new MachineTreeTableCell(this));
        treeView.setColumnFactory(softwareConfiguration, () -> new SoftwareConfigurationTreeTableCell(this));
        treeView.setColumnFactory(machineConfiguration, () -> new MachineConfigurationTreeTableCell(this));
        treeView.setColumnFactory(dateCreated, DateTimeTreeTableCell<Favourite>::new);
        treeView.setColumnFactory(dateModified, DateTimeTreeTableCell<Favourite>::new);
        
        treeView.setOnDragDone(() -> {
            isDragDone = true; // used to prevent the favourite pane to get hidden when cancelling a drag operation by pressing the Escape key
        
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> isDragDone = false));
            timeline.play();
        });
        
        xmlConf = new FavouriteConfiguration();
        treeView.setOnInsertFavourite(() -> saveConfiguration());
        
        selection = treeView.getSelectionModel();
        selection.selectedItemProperty().addListener((o, oV, newItem) -> {
            if (isInserting)
                return;
            show(newItem);
        });
        
        folderList = new ArrayList<>();
        treeView.sceneProperty().addListener((o, oV, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    // wait for CSS styling to get applied then retrieve the folder icons
                    retrieveFolderIcons();
                    initialiseTree();
                }); 
                newScene.getStylesheets().addListener((ListChangeListener.Change<? extends String> c) -> {
                    // a new skin is getting applied (see SkinChoiceField), so must update the folder icons
                    Platform.runLater(() -> retrieveFolderIcons());
                });
            }
        });
        anonymousIcon = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/icon/Negatron.png"));
        
        treeView.getParent().setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE && treeView.isCutCopying()) {
                treeView.resetCutCopy();
                event.consume();
            }
        });
        treeView.setOnKeyPressed(event -> handleKeyPressed(event));
        treeView.setOnKeyReleased(event -> handleKeyReleased(event));
        
        listDivider = favouriteSplitPane.getDividers().get(0);
        listDivider.setPosition(1.0);
        listDivider.positionProperty().addListener(o -> {
            // Fix for a JavaFX resize bug when maximising the main window
            if (listDivider.getPosition() != 1.0)
                bottomPlaceholder.setMaxHeight(USE_COMPUTED_SIZE);
            else
                bottomPlaceholder.setMaxHeight(0.0);
        });
        
        Timeline filterTimeline = new Timeline(new KeyFrame(Duration.millis(300), event -> {
            treeView.beginTreeWiseOperation();
            SortableTreeItem<Favourite> root = (SortableTreeItem<Favourite>) treeView.getRoot();
            
            if (Strings.isValid(filterField.getText())) {
                String filter = filterField.getText().replace(" ", "").toLowerCase();
                root.setPredicate(TreeItemPredicate.create(
                    favourite -> favourite.getShortcut().contains(filter)
                ));
            } else
                root.setPredicate(null);
            
            treeView.endTreeWiseOperation();
        }));
        filterField.textProperty().addListener((o, oV, nV) -> filterTimeline.playFromStart());
    }
    
    private void initialiseTree() {
        if (initialTree == null || folderIcon == null)
            return;
        
        folderList = initialTree.getFolderList();
        folderList.forEach(
            folder -> folder.setIcon(folderIcon)
        );
        initialTree.getEmptyFavouriteList().forEach(
            favourite -> favourite.setIcon(anonymousIcon)
        );
        
        TreeItem<Favourite> root = initialTree.getRoot();
        if (root != null) {
            root.setExpanded(true);
            root.getValue().setIcon(rootIcon);
            treeView.setRoot(root);
        }
        
        initialTree = null;
    }
    
    public void setFavouriteTree(FavouriteTree favourites) {
        initialTree = favourites;
        Platform.runLater(() -> initialiseTree());
    }
    
    public void setEditController(EditController controller) {
        editController = controller;
    }
    
    public void setHoveredCell(FavouriteTreeTableCell hoveredCell) {
        this.hoveredCell = hoveredCell;
    }
    
    public void setEditingCell(FavouriteTreeTableCell editingCell) {
        this.editingCell = editingCell;
    }
    
    public boolean isEditing() {
        return editingCell != null;
    }
    
    public boolean isEditingMachine() {
        return editingCell != null && editingCell instanceof MachineTreeTableCell;
    }
    
    public boolean isEditingSoftware() {
        return editingCell != null && editingCell instanceof SoftwareConfigurationTreeTableCell;
    }
    
    public boolean isEditingConfiguration() {
        return editingCell != null && editingCell instanceof MachineConfigurationTreeTableCell;
    }
    
    public void setOnCommitted(Consumer<Favourite> onCommitted) {
        this.onCommitted = onCommitted;
    }
    
    public void fireOnCommitted() {
        onCommitted.accept(selection.getSelectedItem().getValue());
    }
    
    public void setCommitting(boolean comitting) {
        this.committing = comitting;
    }
    
    public boolean isCommitting() {
        return committing;
    }
    
    public void setOnAction(Delegate delegate) {
        treeView.setOnAction(delegate);
    }
    
    public void saveConfiguration() {
        xmlConf.save(treeView.getRoot());
    }
    
    private void retrieveFolderIcons() {
        Label foldersTabLabel = (Label) treeView.getScene().getRoot().lookup(
            ".titled-pane .tab-pane > .tab-header-area > .headers-region > .tab.folders > .tab-container > .tab-label"
        );
        Image foldersTabIcon = ((ImageView) foldersTabLabel.getGraphic()).getImage();
        if (foldersTabIcon != folderIcon) {
            folderIcon = foldersTabIcon;
            folderList.forEach(
                folder -> folder.setIcon(folderIcon)
            );
        }
        
        ToggleButton favouriteViewButton = (ToggleButton) treeView.getScene().getRoot().lookup(
            ".favourite-view-button"
        );
        Image favouriteViewIcon = ((ImageView) favouriteViewButton.getGraphic()).getImage();
        if (favouriteViewIcon != rootIcon) {
            rootIcon = favouriteViewIcon;
            if (treeView.getRoot() != null) {
                Favourite root = treeView.getRoot().getValue();
                root.setIcon(rootIcon);
            }
        }
    }
    
    private void insert(TreeItem<Favourite> favourite) {
        CopyPastableTreeItem selected = (CopyPastableTreeItem) selection.getSelectedItem();
        if (selected != null) {
            if (selected.getValue() instanceof Folder) {
                if (!selected.isExpanded())
                    selected.setExpanded(true);
                treeView.insert(selected, favourite, Position.INSIDE);
            } else {
                selection.clearSelection();
                treeView.insert(selected, favourite, Position.BEFORE);
                selection.select(favourite);
            }
        } else {
            CopyPastableTreeItem root = (CopyPastableTreeItem) treeView.getRoot();
            if (root == null) {
                root = new CopyPastableTreeItem(new Folder(Language.Manager.getString("favourites"), rootIcon));
                root.setExpanded(true);
                treeView.setRoot(root);
            }
            treeView.insert(root, favourite, Position.INSIDE);
            selection.select(favourite);
        }
    }

    public void insert(Machine currentMachine, SoftwareConfiguration currentSoftware) {
        isInserting = true;
        
        TreeItem<Favourite> favourite = new CopyPastableTreeItem(new Favourite(currentMachine, currentSoftware));
        insert(favourite);
        
        if (selection.getSelectedItem() != favourite)
            selection.select(favourite);
        
        isInserting = false;
        
        treeView.edit(selection.getSelectedIndex(), iconName);
    }
    
    public void requestTreeFocus() {
        treeView.requestFocus();
    }
    
    public void show(TreeItem<Favourite> item) {
        if (item != null) {
            Favourite fav = item.getValue();
            editController.show(fav.getMachine(), fav.getSoftwareConfiguration(), fav.getMachineConfiguration(), fav.mustMigrate());
        } else
            editController.show(null, null, null, false);
    }
    
    private void cancelEdit(Delegate beforeCancellingDelegate) {
        if (treeView.getEditingCell() != null) {
            if (beforeCancellingDelegate != null)
                beforeCancellingDelegate.fire();
            treeView.cancelEdit();
        }
    }
    
    public void cancelEdit() {
        cancelEdit(null);
    }
    
    private void setRowEdit(Machine machine, SoftwareConfiguration software, MachineConfiguration configuration) {
        editingCell.getTreeTableRow().getChildrenUnmodifiable().stream().filter(
            cell -> cell instanceof InteractiveTreeTableCell
        ).map(
            cell -> (InteractiveTreeTableCell) cell
        ).forEach(
            cell -> cell.setEdit(machine, software, configuration)
        );
    }
    
    @Override
    public void changed(Machine newMachine, SoftwareConfiguration newSoftware) {
        setRowEdit(newMachine, newSoftware, MachineConfiguration.buildFromParameters(newMachine));
    }
    
    private void showList(Delegate requestList) {
        if (listTimeline != null)
            listTimeline.stop();
        
        listDividerGrabber = favouriteSplitPane.lookup(".split-pane-divider");
        listDividerGrabber.setMouseTransparent(true);
        
        requestList.fire();
        listTimeline = new Timeline(
            new KeyFrame(
                Duration.millis(200),
                e -> listDividerGrabber.getStyleClass().add("padding"),
                new KeyValue(listDivider.positionProperty(), 0.5)
            )
        );
        listTimeline.play();
    }
    
    private void hideList(Delegate dismissList) {
        if (listDividerGrabber != null) {
            if (listTimeline != null)
                listTimeline.stop();
            
            listDividerGrabber.getStyleClass().remove("padding");
            listDividerGrabber = null;
            
            listTimeline = new Timeline(
                new KeyFrame(
                    Duration.millis(200),
                    new KeyValue(listDivider.positionProperty(), 1.0)
                )
            );
            listTimeline.play();
        }
        
        if (dismissList != null)
            dismissList.fire();
    }
    
    public void hideListBackground() {
        hideList(null);
    }
    
    public void showMachineList() {
        showList(() -> editController.requestMachineList(this, selection.getSelectedItem().getValue().getSoftwareConfiguration()));
    }
    
    public void hideMachineList() {
        isHidingMachineList = true;
        hideList(() -> editController.dismissMachineList(this));
    }
    
    public void showSoftwareList() {
        if (!isEditingMachine())
            showList(() -> editController.requestSoftwareList(this));
        else
            editController.requestSoftwareList(this);
    }
    
    public void hideSoftwareList() {
        if (isHidingMachineList || isEditingMachine()) {
            isHidingMachineList = false;
            editController.dismissSoftwareList(this);
        } else
            hideList(() -> editController.dismissSoftwareList(this));
    }
    
    public void showConfigurationPane() {
        editController.requestConfigurationPane(this, selection.getSelectedItem().getValue().getSoftwareConfiguration());
    }
    
    public void hideConfigurationPane() {
        editController.dismissConfigurationPane(this);
    }

    @FXML
    private void handleNewFolderAction(ActionEvent event) {
        Folder folder = new Folder(Language.Manager.getString("newFolder"), folderIcon);
        folderList.add(folder);
        insert(new CopyPastableTreeItem(folder));
    }

    @FXML
    private void handleNewFavouriteAction(ActionEvent event) {
        Favourite favourite = new Favourite(Language.Manager.getString("newFavourite"), anonymousIcon);
        insert(new CopyPastableTreeItem(favourite));
    }

    @FXML
    private void handleNewSeparatorAction(ActionEvent event) {
        Separator separator = new Separator();
        insert(new CopyPastableTreeItem(separator));
    }

    @FXML
    private void handleExpandAllAction(ActionEvent event) {
        treeView.expandAll();
    }

    @FXML
    private void handleCollapseAllAction(ActionEvent event) {
        treeView.collapseAll();
    }

    @FXML
    private void handleCutAction(ActionEvent event) {
        treeView.cutCopy(CutCopyState.Cut);
    }

    @FXML
    private void handleCopyAction(ActionEvent event) {
        treeView.cutCopy(CutCopyState.Copied);
    }

    @FXML
    private void handlePasteAction(ActionEvent event) {
        treeView.paste();
    }

    @FXML
    private void handleEditAction(ActionEvent event) {
        TreeItem<Favourite> selected = selection.getSelectedItem();
        if (selected != null && !(selected.getValue() instanceof Separator)) {
            TreeTableColumn<Favourite, ?> editingColumn;
            TreeTablePosition<Favourite, ?> position = treeView.getEditingCell();
            if (position != null) {
                if (!(selected.getValue() instanceof Folder)) {
                    // Cycle through columns
                    int columnIdx = treeView.getVisibleLeafIndex(position.getTableColumn());
                    do {
                        if (!isShiftPressed)
                            if (columnIdx < treeView.getVisibleLeafColumns().size() - 1)
                                ++columnIdx;
                            else
                                columnIdx = 0;
                        else
                            if (columnIdx > 0)
                                --columnIdx;
                            else
                                columnIdx = treeView.getVisibleLeafColumns().size() - 1;
                        editingColumn = treeView.getVisibleLeafColumn(columnIdx);
                    } while (
                        editingColumn == dateCreated ||
                        editingColumn == dateModified ||
                        editingColumn == softwareConfiguration && selected.getValue().getSoftwareConfiguration() == null ||
                        editingColumn == machineConfiguration && !selected.getValue().getMachine().isConfigurable()
                    );
                } else
                    return; // Folders only have 1 editable column so no need to change the editing field
            } else
                editingColumn = iconName;
            treeView.edit(selection.getSelectedIndex(), editingColumn);
        }
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        List<TreeItem<Favourite>> list = new ArrayList<>(selection.getSelectedItems()); // shallow copy
        
        if (list.size() > 0) {
            TreeItem<Favourite> selected = selection.getSelectedItem();
            
            // Search for the item that will become the new selection after the deletion operation
            TreeItem<Favourite> ref = selected.nextSibling();
            while (list.contains(ref))
                ref = ref.nextSibling();
            if (ref == null) {
                ref = selected.previousSibling();
                while (list.contains(ref))
                    ref = ref.previousSibling();
                if (ref == null)
                    ref = selected.getParent();
            }
            
            // Remove selected items
            selection.clearSelection();

            list.stream().filter(
                item -> item != null && item.getParent() != null
            ).forEach(
                item -> ((CopyPastableTreeItem) item.getParent()).getInternalChildren().remove(item)
            );

            selection.select(ref);
            saveConfiguration();
        }
    }
    
    private void handleKey(KeyEvent event) {
        if (hoveredCell == null)
            return;
        if (!event.isAltDown() && hoveredCell.getTreeTableRow().isSelected() && !hoveredCell.isEditing() && hoveredCell.canEdit())
            hoveredCell.pseudoClassStateChanged(EDITABLE_CLASS, true);
        else
            hoveredCell.pseudoClassStateChanged(EDITABLE_CLASS, false);
    }
    
    private void handleKeyPressed(KeyEvent event) {
        handleKey(event);
        
        // Disable the default TreeView's CancelEdit handler on Escape key !pressed! events.
        // This is handled by editing fields' cancelEdit() listeners on Escape key !released! events,
        // which should also disable the default TreeView's CancelEdit handler on Escape key !released! events.
        switch (event.getCode()) {
            case ESCAPE:
            case F2:
                event.consume();
                break;
            case SHIFT:
                isShiftPressed = true;
                break;
            default:
                break;
        }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        handleKey(event);
        
        final KeyCombination cutKeyCombo = new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination altCutKeyCombo = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN);
        final KeyCombination copyKeyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination altCopyKeyCombo = new KeyCodeCombination(KeyCode.INSERT, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination pasteKeyCombo = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination altPasteKeyCombo = new KeyCodeCombination(KeyCode.INSERT, KeyCombination.SHIFT_DOWN);
        
        final KeyCombination editKey = new KeyCodeCombination(KeyCode.F2);
        final KeyCombination altEditKey = new KeyCodeCombination(KeyCode.F2, KeyCodeCombination.SHIFT_DOWN);
        final KeyCombination newFolderKey = new KeyCodeCombination(KeyCode.INSERT);
        final KeyCombination newFavouriteKeyCombo = new KeyCodeCombination(KeyCode.INSERT, KeyCodeCombination.ALT_DOWN);
        final KeyCombination newSeparatorKeyCombo = new KeyCodeCombination(KeyCode.INSERT, KeyCodeCombination.ALT_DOWN, KeyCodeCombination.SHIFT_DOWN);
        final KeyCombination deleteKey = new KeyCodeCombination(KeyCode.DELETE);
        
        if (event.getCode() == KeyCode.SHIFT)
            isShiftPressed = false;

        if (event.getCode() == KeyCode.ESCAPE) {
            if (isDragDone) {
                event.consume();
                return;
            }
            // Still override the default TreeView's CancelEdit handler on Escape key !released! events
            // in the advent that editing fields' cancelEdit() listeners don't override it in every case.
            // As it propagates the event to pane closing listeners by default, the below code block stops it from doing so.
            cancelEdit(() -> {
                if (editingCell != null && editingCell.isEditing())
                    event.consume(); // stop event propagation
            });
        } else if (cutKeyCombo.match(event) || altCutKeyCombo.match(event)) {
            cutButton.fire();
            event.consume();
        } else if (copyKeyCombo.match(event) || altCopyKeyCombo.match(event)) {
            copyButton.fire();
            event.consume();
        } else if (pasteKeyCombo.match(event) || altPasteKeyCombo.match(event)) {
            pasteButton.fire();
            event.consume();
        } else if (newFolderKey.match(event)) {
            newFolderButton.fire();
            event.consume();
        } else if (newFavouriteKeyCombo.match(event)) {
            newFavouriteButton.fire();
            event.consume();
        } else if (newSeparatorKeyCombo.match(event)) {
            newSeparatorButton.fire();
            event.consume();
        } else if (deleteKey.match(event)) {
            if (treeView.getEditingCell() == null || !editingCell.isEditing()) {
                deleteButton.fire();
                event.consume();
            }
        } else if (editKey.match(event) || altEditKey.match(event)) {
            editButton.fire();
            event.consume();
        }
    }
}
