/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2025 BabelSoft S.A.S.U.
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Xiny
 */
public class ScrollTextFlow extends StackPane {

    private final TextFlow textFlow;
    private final ScrollPane scrollPane;
    int selectionStartIndex = -1, selectionEndIndex = -1;
    int characterCount;
    
    public ScrollTextFlow() {
        textFlow = new TextFlow();
        textFlow.setPadding(new Insets(10));
        textFlow.setTextAlignment(TextAlignment.JUSTIFY);
        textFlow.setOnMouseReleased(this::setOnMouseReleased);
        textFlow.setOnMousePressed(this::setOnMousePressed);
        textFlow.setOnMouseDragged(this::setOnMouseDragged);
        textFlow.setOnKeyPressed(this::setOnKeyPressed);
        
        scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.vvalueProperty().addListener((o, oV, nV) -> {
            List<Node> children = getChildren();
            if (children.size() > 1)
                scrollNode(children.getFirst());
        });
        
        getChildren().add(scrollPane);
        getStyleClass().add("textFlowPane");
        setAlignment(Pos.TOP_LEFT);
        characterCount = 0;
    }
    
    public void addNode(Node node) {
        textFlow.getChildren().add(node);
        switch (node) {
            case Text text -> characterCount += text.getText().length();
            case Hyperlink link -> ++characterCount;
            default -> { }
        }
    }
    
    public void autoTail() {
        // make the pane to always scroll automatically to the bottom
        scrollPane.vvalueProperty().bind(textFlow.heightProperty());
    }
    
    private void scrollNode(Node node) {
        // MinY would normally be retrieved from scrollPane.getViewportBounds().getMinY()
        // but this property is never updated in release mode... only in debug mode.
        double range = scrollPane.getVmax() - scrollPane.getVmin();
        double minY = scrollPane.getVvalue() / range * (
            textFlow.getLayoutBounds().getHeight() - scrollPane.getViewportBounds().getHeight()
        );
        node.setTranslateY(textFlow.getPadding().getTop() - minY);
    }
    
    private void selectText() {
        final int startIndex = selectionStartIndex < selectionEndIndex ? selectionStartIndex : selectionEndIndex;
        final int endIndex = selectionStartIndex < selectionEndIndex ? selectionEndIndex : selectionStartIndex;

        PathElement[] elements = textFlow.rangeShape(startIndex, endIndex);
        Path path = new Path(elements);
        path.getStyleClass().add("selection-path");
        path.setManaged(false);

        path.setTranslateX(textFlow.getPadding().getLeft());
        scrollNode(path);

        List<Node> children = getChildren();
        if (children.size() > 1)
            children.removeFirst();
        children.addFirst(path);

        // text.setSelectionStart/End() both only work for the very first text node...
        // TODO uncomment below block when JavaFX will be able to manage selection on several text nodes simultaneously.
        /*int index = 0;
        boolean foundSelection = false;
        nodeLoop: for (Node node : textFlow.getChildrenUnmodifiable()) switch (node) {
            case Text text -> {
                final String currentText = text.getText();
                if (!foundSelection) {
                    if (index <= startIndex && startIndex < index + currentText.length()) {
                        text.setSelectionStart(startIndex - index);
                        if (endIndex - index < currentText.length()) {
                            text.setSelectionEnd(endIndex - index);
                            break nodeLoop;
                        } else
                            text.setSelectionEnd(currentText.length());
                        foundSelection = true;
                    }
                } else {
                    text.setSelectionStart(0);
                    if (endIndex < index + currentText.length()) {
                        text.setSelectionEnd(endIndex - index);
                        break nodeLoop;
                    } else
                        text.setSelectionEnd(currentText.length());
                }
                index += currentText.length();
            }
            case Hyperlink link -> ++index;
            default -> { }
        }*/
    };
                    
    private void setOnMouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            // by default this node never gets the focus.
            // So, to be able to catch keyboard events, force the focus on it
            textFlow.requestFocus();

            double x = e.getX() - textFlow.getPadding().getLeft();
            double y = e.getY() - textFlow.getPadding().getTop();
            int selectionIndex = textFlow.hitTest(new Point2D(x, y)).getCharIndex();

            int index = 0;
            nodeLoop: for (Node node : textFlow.getChildrenUnmodifiable()) switch (node) {
                case Text text -> {
                    final String currentText = text.getText();
                    if (index <= selectionIndex && selectionIndex < index + currentText.length()) {
                        selectionIndex -= index;

                        for (selectionStartIndex = selectionIndex - 1; 0 < selectionStartIndex; --selectionStartIndex)
                            if (Character.toString(currentText.charAt(selectionStartIndex)).isBlank())
                                break;
                        for (selectionEndIndex = selectionIndex + 1; selectionEndIndex < currentText.length(); ++selectionEndIndex)
                            if (Character.toString(currentText.charAt(selectionEndIndex)).isBlank())
                                break;
                        if (selectionStartIndex < 0)
                            selectionStartIndex = 0;
                        else if (Character.toString(currentText.charAt(selectionStartIndex)).isBlank())
                            ++selectionStartIndex; // ignore initial blank
                        selectionStartIndex += index;
                        selectionEndIndex += index;

                        selectText();
                        break nodeLoop;
                    }
                    index += currentText.length();
                }
                case Hyperlink link -> ++index;
                default -> { }
            }

            e.consume();
        }
    }
    
    private void setOnMousePressed(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            if (e.isShiftDown()) {
                setOnMouseDragged(e);
                return;
            }
                
            // by default this node never gets the focus.
            // So, to be able to catch keyboard events, force the focus on it
            textFlow.requestFocus();

            double x = e.getX() - textFlow.getPadding().getLeft();
            double y = e.getY() - textFlow.getPadding().getTop();
            selectionStartIndex = textFlow.hitTest(new Point2D(x, y)).getCharIndex();
            selectionEndIndex = -1;

            List<Node> children = getChildren();
            if (children.size() > 1)
                children.removeFirst();

            textFlow.getChildrenUnmodifiable().forEach(node -> {
                if (node instanceof Text text)
                    text.setSelectionStart(-1);
            });

            e.consume();
        }
    }
    
    private void setOnMouseDragged(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            double x = e.getX() - textFlow.getPadding().getLeft();
            double y = e.getY() - textFlow.getPadding().getTop();
            selectionEndIndex = textFlow.hitTest(new Point2D(x, y)).getCharIndex();
            selectText();
            e.consume();
        }
    }
    
    private void setOnKeyPressed(KeyEvent e) {
        final KeyCombination selectAllKeyCombo = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination copyKeyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination rightKeyCombo = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);
        final KeyCombination leftKeyCombo = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
        final KeyCombination rightCtrlKeyCombo = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);
        final KeyCombination leftCtrlKeyCombo = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);        
        if (copyKeyCombo.match(e)) {
            int index = 0;
            final int startIndex = selectionStartIndex < selectionEndIndex ? selectionStartIndex : selectionEndIndex;
            final int endIndex = selectionStartIndex < selectionEndIndex ? selectionEndIndex : selectionStartIndex;
            StringBuilder selectedText = new StringBuilder();

            nodeLoop: for (Node node : textFlow.getChildrenUnmodifiable()) switch (node) {
                case Text text -> {
                    final String currentText = text.getText();
                    if (selectedText.isEmpty()) {
                        if (index <= startIndex && startIndex < index + currentText.length()) {
                            if (endIndex - index < currentText.length()) {
                                selectedText.append(currentText.substring(startIndex - index, endIndex - index));
                                break nodeLoop;
                            } else
                                selectedText.append(currentText.substring(startIndex - index, currentText.length()));
                        }
                    } else {
                        if (endIndex < index + currentText.length()) {
                            selectedText.append(currentText.substring(0, endIndex - index));
                            break nodeLoop;
                        } else
                            selectedText.append(currentText.substring(0, currentText.length()));
                    }
                    index += currentText.length();
                }
                case Hyperlink link -> {
                    selectedText.append(link.getText());
                    ++index;
                }
                default -> { }
            }

            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(selectedText.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);

            e.consume();
        } else if (selectAllKeyCombo.match(e)) {
            selectionStartIndex = 0;
            selectionEndIndex = characterCount;
            selectText();
            e.consume();
        } else if (leftKeyCombo.match(e) || rightKeyCombo.match(e)) {
            e.consume();
            
            if (e.getCode() == KeyCode.LEFT) {
                if (selectionEndIndex > 0)
                    --selectionEndIndex;
                else
                    return;
            } else { // e.getCode() == KeyCode.RIGHT
                if (selectionEndIndex < characterCount)
                    ++selectionEndIndex;
                else
                    return;
            }
            
            selectText();
        } else if (leftCtrlKeyCombo.match(e) || rightCtrlKeyCombo.match(e)) {
            e.consume();
            
            if (e.getCode() == KeyCode.LEFT && selectionEndIndex == 0)
                return;
            if (e.getCode() == KeyCode.RIGHT && selectionEndIndex == characterCount)
                return;
            
            int index = 0;
            final int referenceIndex = selectionEndIndex;
                
            BiFunction<String, Integer, Boolean> findWordBound = (text, idx) -> {
                if (idx <= referenceIndex && referenceIndex <= idx + text.length()) {
                    int selectionIndex = referenceIndex - idx;

                    if (e.getCode() == KeyCode.RIGHT) {
                        for (selectionEndIndex = selectionIndex + 1; selectionEndIndex < text.length(); ++selectionEndIndex)
                            if (Character.toString(text.charAt(selectionEndIndex)).isBlank())
                                break;
                    } else { // KeyCode.LEFT
                        for (selectionEndIndex = selectionIndex - 1; selectionEndIndex > 0; --selectionEndIndex)
                            if (Character.toString(text.charAt(selectionEndIndex)).isBlank())
                                break;
                    }
                    selectionEndIndex += idx;

                    selectText();
                    return true;
                }
                return false;
            };
            
            nodeLoop: for (Node node : textFlow.getChildrenUnmodifiable()) {
                String currentText = "";
                
                switch (node) {
                    case Text text -> {
                        currentText = text.getText();
                        if (findWordBound.apply(currentText, index))
                            break nodeLoop;
                    }
                    case Hyperlink link -> {
                        currentText = "h"; // dummy non-blank single character text
                        if (findWordBound.apply(currentText, index))
                            break nodeLoop;
                    }
                    default -> { }
                }
                
                index += currentText.length();
            }
        }
    }
}
