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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.Folder;
import net.babelsoft.negatron.model.favourites.Separator;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.ReversedIterator;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.tree.CopyPastableTreeItem;
import net.babelsoft.negatron.view.control.tree.CopyPastableTreeItem.CutCopyState;
import net.babelsoft.negatron.view.control.tree.DisclosureNode;
import net.babelsoft.negatron.view.skin.FavouriteTreeViewSkin;

/**
 * For drag'n drop of tree rows, used some code from http://programmingtipsandtraps.blogspot.fr/2015/10/drag-and-drop-in-treetableview-with.html
 * @author capan
 */
public class FavouriteTreeView extends NegatronTreeView<Favourite> {

    public static enum Position {
        NONE,
        BEFORE,
        AFTER,
        INSIDE;
    }
    
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private static final PseudoClass CSS_CUT = PseudoClass.getPseudoClass("cut");
    private static final PseudoClass CSS_COPIED = PseudoClass.getPseudoClass("copied");
    private static final PseudoClass CSS_INVALIDATED = PseudoClass.getPseudoClass("invalidated");
    private static final PseudoClass CSS_MOVE_BEFORE = PseudoClass.getPseudoClass("move-before");
    private static final PseudoClass CSS_MOVE_AFTER = PseudoClass.getPseudoClass("move-after");
    private static final PseudoClass CSS_MOVE_INSIDE = PseudoClass.getPseudoClass("move-inside");
    
    private final Timeline scrollTimeline = new Timeline();
    private double scrollDirection = 0;
    
    private List<CopyPastableTreeItem> cutCopyList;
    private Delegate onInsertFavourite;
    private Delegate onDragDone;
    private boolean isDragDropping;
    
    private PopOver popup;
    
    TreeTableColumn<Favourite, ?> nonConfigurableColumn;
    private Function<TreeTablePosition<Favourite, ?>, Boolean> isCellEditable;
    
    public enum Cycle {
        LOOK_FORWARD,
        LOOK_BACKWARD
    }
    
    private Cycle fieldJumpMode = Cycle.LOOK_FORWARD;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setRowFactory(tree -> {
            TreeTableRow<Favourite> cell = new TreeTableRow<>();
            
            // Manage visual cues with CSS pseudo classes
            ChangeListener<CutCopyState> cutCopyStateListener = (o, oV, cutState) -> {
                cell.pseudoClassStateChanged(CSS_CUT, !isDragDropping && cutState == CutCopyState.Cut);
                cell.pseudoClassStateChanged(CSS_COPIED, !isDragDropping && cutState == CutCopyState.Copied);
            };
            ChangeListener<Boolean> invalidatedListener = (o, oV, invalidated) -> {
                cell.pseudoClassStateChanged(CSS_INVALIDATED, invalidated);
                if (!invalidated) {
                    Favourite favourite = cell.getItem();
                    if (favourite != null && favourite.getMachine() != null && favourite.getMachine().getIconDescription() != null)
                        favourite.setIcon(favourite.getMachine().getIconDescription().getIcon());
                }
            };
            cell.treeItemProperty().addListener((o, oldItem, newItem) -> {
                if (oldItem != null) {
                    ((CopyPastableTreeItem) oldItem).cutCopyStateProperty().removeListener(cutCopyStateListener);
                    ReadOnlyBooleanProperty invalidated = oldItem.getValue().invalidatedProperty();
                    if (invalidated != null)
                        invalidated.removeListener(invalidatedListener);
                }
                if (newItem != null) {
                    CopyPastableTreeItem item = (CopyPastableTreeItem) newItem;
                    item.cutCopyStateProperty().addListener(cutCopyStateListener);
                    cutCopyStateListener.changed(null, null, item.getCutCopyState());
                    
                    ReadOnlyBooleanProperty invalidated = item.getValue().invalidatedProperty();
                    if (invalidated != null) {
                        invalidated.addListener(invalidatedListener);
                        invalidatedListener.changed(null, null, item.getValue().isInvalidated());
                    }
                } else {
                    cutCopyStateListener.changed(null, null, CutCopyState.None);
                    invalidatedListener.changed(null, null, false);
                }
            });
            
            // Manage drag'n drop
            cell.setOnDragDetected(event -> {
                isDragDropping = true;
                
                cutCopy(CutCopyState.Cut);
                if (isCutCopying()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(cell.snapshot(null, null));
                    // useless but mandatory section. Without it, drag operations are never taken into account
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, cell.getIndex());
                    db.setContent(cc);
                    
                    event.consume();
                } else
                    isDragDropping = false;
            });
           
            cell.setOnDragOver(event -> {
                if (acceptable(cell)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    Position position = getDragDroppedPosition(cell, event);
                    switch (position) {
                        case BEFORE -> {
                            cell.pseudoClassStateChanged(CSS_MOVE_BEFORE, true);
                            cell.pseudoClassStateChanged(CSS_MOVE_AFTER, false);
                            cell.pseudoClassStateChanged(CSS_MOVE_INSIDE, false);
                        }
                        case AFTER -> {
                            cell.pseudoClassStateChanged(CSS_MOVE_BEFORE, false);
                            cell.pseudoClassStateChanged(CSS_MOVE_AFTER, true);
                            cell.pseudoClassStateChanged(CSS_MOVE_INSIDE, false);
                        }
                        default -> {
                            // INSIDE
                            cell.pseudoClassStateChanged(CSS_MOVE_BEFORE, false);
                            cell.pseudoClassStateChanged(CSS_MOVE_AFTER, false);
                            cell.pseudoClassStateChanged(CSS_MOVE_INSIDE, true);
                        }
                    }
                    event.consume();
                }
            });
            
            cell.setOnDragExited(event -> {
                cell.pseudoClassStateChanged(CSS_MOVE_BEFORE, false);
                cell.pseudoClassStateChanged(CSS_MOVE_AFTER, false);
                cell.pseudoClassStateChanged(CSS_MOVE_INSIDE, false);
                event.consume();
            });
   
            cell.setOnDragDropped(event -> {
                CopyPastableTreeItem selected = (CopyPastableTreeItem) cell.getTreeItem();
                List<CopyPastableTreeItem> processList = new ArrayList<>(cutCopyList.size());
                if (acceptable(selected, processList)) {
                    paste(selected, processList, getDragDroppedPosition(cell, event));
                    event.consume();
                }
            });
            
            cell.setOnDragDone(event -> {
                resetCutCopy();
                isDragDropping = false;
                onDragDone.fire();
            });

            // Add disclosure
            final StackPane disclosureNode = new DisclosureNode(cell);
            cell.setDisclosureNode(disclosureNode);
            
            return cell;
        });
        
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setupScrolling();
    }

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new FavouriteTreeViewSkin(this);
    }
    
    //////////////////////// Drag'n drop management
    
    private boolean acceptable(TreeTableRow<Favourite> cell) {
        return acceptable(
            (CopyPastableTreeItem) cell.getTreeItem(), new ArrayList<>(cutCopyList.size())
        );
    }
    
    private boolean acceptable(CopyPastableTreeItem selected, List<CopyPastableTreeItem> processList) {
        if (cutCopyList == null)
            return false; // no previous cut/copy operations performed
        
        if (selected == null)
            return false; // no destination row selected

        final CutCopyState cutCopyState = cutCopyList.get(0).getCutCopyState();

        if (cutCopyState == CutCopyState.Cut && cutCopyList.stream().filter(item -> {
            if (item == selected)
                return false;
            for (TreeItem<Favourite> parent = selected.getParent(); parent != null; parent = parent.getParent())
                if (parent == item)
                    return true;
            return false;
        }).findAny().isPresent())
            return false; // destination row cannot be the child of one of the rows to move/copy

        processList.addAll(cutCopyList);

        // Remove the selected item from the process list (as it's already in place)
        if (cutCopyState == CutCopyState.Cut)
            processList.removeIf(item -> item == selected);
        // Also remove items for which any parent folders are also in the cut copy list (as the process will move the entire parent folders' subtrees anyway)
        cutCopyList.stream().filter(
            item -> item.getValue() instanceof Folder
        ).forEach(folder -> {
            processList.removeIf(item -> {
                for (TreeItem<Favourite> parent = item.getParent(); parent != null; parent = parent.getParent())
                    if (parent == folder)
                        return true;
                return false;
            });
        });
        
        return !processList.isEmpty();
    }

    private CopyPastableTreeItem getTarget(TreeTableRow<Favourite> row) {
        CopyPastableTreeItem target = (CopyPastableTreeItem) getRoot();
        if (!row.isEmpty()) {
            target = (CopyPastableTreeItem) row.getTreeItem();
        }
        return target;
    }

    // Prevent loops in the tree
    private boolean isParent(TreeItem parent, TreeItem child) {
        boolean result = false;
        while (!result && child != null) {
            result = child.getParent() == parent;
            child = child.getParent();
        }
        return result;
    }
    
    private void setupScrolling() {
        scrollTimeline.setCycleCount(Timeline.INDEFINITE);
        scrollTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), e -> dragScroll()));
        setOnDragExited(event -> {
            double x = event.getX(), y = event.getY();
            if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                if (y > 0) {
                    scrollDirection = 1.0 / getExpandedItemCount();
                }
                else {
                    scrollDirection = -1.0 / getExpandedItemCount();
                }
                scrollTimeline.play();
            } else // workaround for Linux and macOS, where the event DragDone never gets triggered
                scrollTimeline.stop();
        });
        setOnDragEntered(event -> scrollTimeline.stop());
        setOnDragDone(event -> scrollTimeline.stop());
    }

    private void dragScroll() {
        ScrollBar sb = null;
        for (Node n : lookupAll(".scroll-bar"))
            if (n instanceof ScrollBar bar && bar.getOrientation().equals(Orientation.VERTICAL))
                sb = bar;
        
        if (sb != null) {
            double newValue = sb.getValue() + scrollDirection;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            sb.setValue(newValue);
        }
    }
    
    private static Position getDragDroppedPosition(TreeTableRow<Favourite> cell, DragEvent event) {
        double position = event.getY();
        double limit = cell.getHeight();
        if (cell.getItem() instanceof Folder) {
            limit /= 3.0;
            if (position <= limit)
                return Position.BEFORE;
            else if (position <= limit * 2.0)
                return Position.INSIDE; // default behaviour with copy-pasting
            else
                return Position.AFTER;
        } else {
            limit /= 2.0;
            if (position <= limit)
                return Position.BEFORE; // default behaviour with copy-pasting
            else
                return Position.AFTER;
        }
    }
    
    //////////////////////// Cut'n paste management
    
    public void insert(CopyPastableTreeItem reference, TreeItem<Favourite> favourite, Position position) {
        switch (position) {
            case BEFORE:
                CopyPastableTreeItem parent = (CopyPastableTreeItem) reference.getParent();
                ObservableList<TreeItem<Favourite>> list = parent.getInternalChildren();
                list.add(list.indexOf(reference), favourite);
                break;
            case INSIDE:
                reference.getInternalChildren().add(favourite);
                break;
            case AFTER:
            default:
                return;
        }
        onInsertFavourite.fire();
    }
    
    public boolean isCutCopying() {
        return cutCopyList != null;
    }
    
    public void resetCutCopy() {
        if (cutCopyList != null) {
            cutCopyList.stream().map(
                item -> (CopyPastableTreeItem) item
            ).forEach(
                item -> item.setCutCopyState(CutCopyState.None)
            );
            cutCopyList = null;
        }
    }
    
    public void cutCopy(CutCopyState state) {
        resetCutCopy();
        ObservableList<TreeItem<Favourite>> list = getSelectionModel().getSelectedItems();
        if (list != null && !list.isEmpty()) {
            cutCopyList = new ArrayList<>(list.size());
            TreeItem<Favourite> root = getRoot();

            list.stream().filter(
                item -> item != root // the root node can't be moved away from its position, so exclude it
            ).map(
                item -> (CopyPastableTreeItem) item
            ).forEach(
                item -> {
                    cutCopyList.add(item);
                    item.setCutCopyState(state);
                }
            );

            if (cutCopyList.isEmpty())
                cutCopyList = null; // nothing to do
        }
    }
    
    public void paste() {
        CopyPastableTreeItem selected = (CopyPastableTreeItem) getSelectionModel().getSelectedItem();
        List<CopyPastableTreeItem> processList = new ArrayList<>();

        if (acceptable(selected, processList)) {
            if (selected.getValue() instanceof Folder)
                paste(selected, processList, Position.INSIDE);
            else
                paste(selected, processList, Position.BEFORE);
        }

        resetCutCopy();
    }
    
    private void paste(CopyPastableTreeItem selected, List<CopyPastableTreeItem> processList, Position position) {
        final CutCopyState cutCopyState = cutCopyList.get(0).getCutCopyState();
        getSelectionModel().clearSelection();
        switch (position) {
            case BEFORE -> {
                CopyPastableTreeItem parent = (CopyPastableTreeItem) selected.getParent();
                ObservableList<TreeItem<Favourite>> list = parent.getInternalChildren();
                processList.forEach(favourite -> {
                    if (cutCopyState == CutCopyState.Cut)
                        ((CopyPastableTreeItem) favourite.getParent()).getInternalChildren().remove(favourite);
                    else // CutCopyState.Copied
                        favourite = favourite.copy();
                    list.add(list.indexOf(selected), favourite);
                });
            }
            case AFTER -> {
                CopyPastableTreeItem parent = (CopyPastableTreeItem) selected.getParent();
                ObservableList<TreeItem<Favourite>> list = parent.getInternalChildren();
                ReversedIterator.stream(processList).forEachOrdered(favourite -> {
                    if (cutCopyState == CutCopyState.Cut)
                        ((CopyPastableTreeItem) favourite.getParent()).getInternalChildren().remove(favourite);
                    else // CutCopyState.Copied
                        favourite = favourite.copy();
                    list.add(list.indexOf(selected) + 1, favourite);
                });
            }
            case INSIDE -> {
                if (!selected.isExpanded())
                    selected.setExpanded(true);
                processList.forEach(favourite -> {
                    if (cutCopyState == CutCopyState.Cut)
                        ((CopyPastableTreeItem) favourite.getParent()).getInternalChildren().remove(favourite);
                    else // CutCopyState.Copied
                        favourite = favourite.copy();
                    selected.getInternalChildren().add(favourite);
                });
            }
            default -> {
                return;
            }
        }
        if (processList.size() == 1)
            getSelectionModel().select(processList.get(0));
        onInsertFavourite.fire();
        // In some cases, node indentations aren't updated correctly, so I work around this by forcing a crude overall refresh (TODO for Oracle: fix this).
        // As most users should have a tree with less than a few hundred nodes, the operation cost should still be acceptable performance-wise.
        refresh();
    }
    
    //////////////////////// Miscellaneous operations
    
    public <S> void setColumnFactory(
        TreeTableColumn<Favourite, S> col,
        Supplier<? extends TreeTableCell<Favourite, S>> builder
    ) {
        col.setCellValueFactory(new TreeItemPropertyValueFactory<>(col.getId()));
        col.setCellFactory(column -> {
            TreeTableCell<Favourite, S> cell = builder.get();
            cell.setOnMouseClicked(evt -> handleMouseClicked(evt));
            return cell;
        });
    }
    
    public void setOnInsertFavourite(Delegate delegate) {
        onInsertFavourite = delegate;
    }
    
    public void setOnDragDone(Delegate delegate) {
        onDragDone = delegate;
    }
    
    public void setNonConfigurableColumn(TreeTableColumn<Favourite, ?> column) {
        nonConfigurableColumn = column;
    }
    
    public void setIsCellEditable(Function<TreeTablePosition<Favourite, ? extends Object>, Boolean> function) {
        isCellEditable = function;
    }
    
    public void setFieldJumpMode(Cycle mode) {
        fieldJumpMode = mode;
    }
    
    private record Cell(int rowIndex, TreeTableColumn<Favourite, ?> column) {}
    
    private Cell getEditableCell(TreeTablePosition<Favourite, ?> cell, boolean ignoreCurrentCell) {
        List<TreeTableColumn<Favourite, ?>> list = getVisibleLeafColumns();
        int nbCol = list.size();
        int rowIndex = cell.getRow();
        int colIndex = cell.getColumn();
        TreeTableColumn<Favourite, ?> column;
        
        // Field jump cycling: find the next editable cell
        if (!ignoreCurrentCell) {
            column = cell.getTableColumn();
            while (!isCellEditable.apply(cell)) {
                colIndex += fieldJumpMode == Cycle.LOOK_FORWARD ? 1 : -1;
                if (colIndex >= nbCol)
                    colIndex = 0; // cycled through the visible columns forward, so go back to the first
                else if (colIndex < 0)
                    colIndex = nbCol - 1; // cycled through the visible columns backward, so go back to the last
                column = list.get(colIndex);
                cell = new TreeTablePosition<>(this, rowIndex, column);
            }
        } else {
            do {
                colIndex += fieldJumpMode == Cycle.LOOK_FORWARD ? 1 : -1;
                if (colIndex >= nbCol)
                    colIndex = 0; // cycled through the visible columns forward, so go back to the first
                else if (colIndex < 0)
                    colIndex = nbCol - 1; // cycled through the visible columns backward, so go back to the last
                column = list.get(colIndex);
                cell = new TreeTablePosition<>(this, rowIndex, column);
            } while (!isCellEditable.apply(cell));
        }

        getFocusModel().focus(rowIndex, column);
        getSelectionModel().select(rowIndex, column);
        return new Cell(rowIndex, column);
    }
    
    public void edit() {
        TreeTablePosition<Favourite, ?> cell = getFocusModel().getFocusedCell();
        if (cell.getRow() >= 0)
            editCell(cell.getRow(), cell.getTableColumn());
    }
    
    public void editCell(int i, TreeTableColumn<Favourite, ?> ttc) {
        TreeTablePosition<Favourite, ?> cell = getEditingCell();
        if (cell != null) {
            // Already editing a cell, so perform a field jump by passing editing on to the next cell in the same row.
            Cell c = getEditableCell(cell, true);
            if (c.column() != cell.getTableColumn()) {
                cancelEdit();
                edit(c.rowIndex(), c.column());
            }
        } else {
            cell = new TreeTablePosition<>(this, i, ttc);
            switch (cell.getTreeItem().getValue()) {
                case Separator s -> { }
                default -> {
                    Cell c = getEditableCell(cell, false);
                    edit(i, c.column());
                }
            }
        }
        Platform.runLater(this::requestFocus);
    }
    
    @Override
    public void edit(int i, TreeTableColumn<Favourite, ?> ttc) {
        super.edit(i, ttc);
        if (i < 0 || ttc == nonConfigurableColumn)
            editableControl.setEditable(false);
    }
    
    public void cancelEdit() {
        edit(-1, null);
    }

    @Override
    protected void performAllExpanded(List<TreeItem<Favourite>> children, boolean value) {
        children.stream().filter(
            child -> child != null && !child.isLeaf()
        ).forEach(child -> {
            performAllExpanded(child.getChildren(), value);
            child.setExpanded(value);
        });
    }
    
    @Override
    protected void handleMouseClicked(MouseEvent event) {
        @SuppressWarnings("unchecked")
        TreeTableCell<Favourite, ?> cell = (TreeTableCell<Favourite, ?>) event.getSource();
        
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !cell.isEditing())
            cancelEdit();
        else if (event.getButton() == MouseButton.MIDDLE) {
            int index = cell.getIndex();
            TreeTableColumn<Favourite, ?> column = cell.getTableColumn();
            
            TreeTablePosition<Favourite, ?> editingCell = getEditingCell();
            TreeTablePosition<Favourite, ?> eventCell = new TreeTablePosition<>(this, index, column);
            
            if (editingCell != null)
                if (editingCell.equals(eventCell))
                    return; // try to re-edit a cell already being edited
                else
                    cancelEdit();
            
            getFocusModel().focus(index, column);
            getSelectionModel().select(index, column);
            
            if (isCellEditable.apply(eventCell))
                edit(index, column);
        } else if (event.getClickCount() == 1) {
            getSelectionModel().clearAndSelect(cell.getIndex(), cell.getTableColumn());
            editableControl.setEditable(false);
        } else {
            Favourite favourite = cell.getTableRow().getItem();
            if (favourite != null && favourite.getMachine() != null)
                if (!favourite.mustMigrate())
                    super.handleMouseClicked(event);
                else
                    showFavouriteMigrationPopup();
        }
        event.consume();
    }
    
    @Override
    public void handleKeyPressed(KeyEvent event) {
        TreeItem<Favourite> item = getSelectionModel().getSelectedItem();
        if (
                (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ALT) &&
                item != null && item.getValue() != null && item.getValue().mustMigrate()
        ) {
            showFavouriteMigrationPopup();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE)
            event.consume(); // prevent the escape event to be passed on to the pane while cancelling editing
        else
            super.handleKeyPressed(event);
    }
    
    private void showFavouriteMigrationPopup() {
        if (popup == null) {
            popup = new PopOver();
            popup.setAnimated(true);
            popup.setTitle(Language.Manager.getString("favouriteMigration.title"));
            
            Label label = new Label(Language.Manager.getString("favouriteMigration.text"));
            label.setPadding(new Insets(8));
            label.getStyleClass().add("text");
            popup.setContentNode(label);
        }
        int rowIndex = getSelectionModel().getSelectedIndex();
        Set<Node> treeTableRowCell = lookupAll(".tree-table-row-cell");
        for (Node tableRow : treeTableRowCell) {
            TreeTableRow<?> row = (TreeTableRow<?>) tableRow;
            if (row.getIndex() == rowIndex) {
                Set<Node> cells = row.lookupAll(".tree-table-cell");
                for (Node node : cells) {
                    TreeTableCell<?, ?> cell = (TreeTableCell<?, ?>) node;
                    if (getColumns().indexOf(cell.getTableColumn()) == 1) {
                        popup.show(cell);
                        break;
                    }
                }
                break;
            }
        }
    }
}
