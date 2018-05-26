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

import java.util.List;
import java.util.function.Consumer;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import net.babelsoft.negatron.util.function.Delegate;

/**
 *
 * @author capan
 */
public abstract class NegatronTreeView<T> extends TreeTableView<T> implements Initializable {
    
    private Delegate onAction;
    protected TreeItem<T> selectedTreeItem;
    private Consumer<Boolean> onTreeWiseOperation;
    
    public NegatronTreeView() {
        super();
        
        final KeyCombination expandAllKeyCombo = new KeyCodeCombination(KeyCode.MULTIPLY);
        final KeyCombination collapseAllKeyCombo = new KeyCodeCombination(KeyCode.DIVIDE);
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Disable TreeView's native expand all handler on key pressed event
            if (expandAllKeyCombo.match(event)) {
                event.consume();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> handleKeyPressed(event));
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            // Disable TreeView's native expand all handler on key released event
            // and replace it by a more efficient implementation
            if (expandAllKeyCombo.match(event)) {
                expandAll();
                event.consume();
            } else if (collapseAllKeyCombo.match(event)) {
                collapseAll();
                event.consume();
            }
        });
    }
    
    public void setOnAction(Delegate delegate) {
        onAction = delegate;
    }
    
    public final void setOnTreeWiseOperation(Consumer<Boolean> delegate) {
        onTreeWiseOperation = delegate;
    }
    
    public final void beginTreeWiseOperation() {
        if (onTreeWiseOperation != null)
            onTreeWiseOperation.accept(Boolean.TRUE);
        
        selectedTreeItem = getSelectionModel().getSelectedItem();
        getSelectionModel().clearSelection();
    }
    
    public final void endTreeWiseOperation() {
        endTreeWiseOperation(false);
    }
    
    protected final void endTreeWiseOperation(boolean selectParent) {
        if (selectedTreeItem != null) {
            if (selectParent) {
                TreeItem<T> parent = selectedTreeItem.getParent();
                if (parent != getRoot())
                    selectedTreeItem = parent;
            }
            getSelectionModel().select(selectedTreeItem);
            if (getSelectionModel().getSelectedItem() != selectedTreeItem) {
                if (onTreeWiseOperation != null)
                    onTreeWiseOperation.accept(Boolean.FALSE);
                getSelectionModel().clearSelection();
                scrollTo(0);
            } else
                scrollTo(getSelectionModel().getSelectedIndex());
        }
        
        if (onTreeWiseOperation != null)
            onTreeWiseOperation.accept(Boolean.FALSE);
    }
    
    private void setAllExpanded(boolean value) {
        TreeItem<T> root = getRoot();
        if (root == null) return;
        
        beginTreeWiseOperation();
        performAllExpanded(root.getChildren(), value);
        endTreeWiseOperation(!value);
    }
    
    protected abstract void performAllExpanded(List<TreeItem<T>> children, boolean value);
    
    public void expandAll() {
        setAllExpanded(true);
    }
    
    public void collapseAll() {
        setAllExpanded(false);
    }
    
    protected void handleAction() {
        if (onAction != null)
            onAction.fire();
    }
    
    protected void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            handleAction();
            event.consume();
        }
    }
    
    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleAction();
            event.consume();
        }
    }
}
