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
package net.babelsoft.negatron.view.control.tree;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * Package protected - not intended for external use
 * @author Oracle
 */
class CellUtils {
    
    static void requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(Control c) {
        Scene scene = c.getScene();
        final Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner == null) {
            c.requestFocus();
        } else if (! c.equals(focusOwner)) {
            Parent p = focusOwner.getParent();
            while (p != null) {
                if (c.equals(p)) {
                    c.requestFocus();
                    break;
                }
                p = p.getParent();
            }
        }
    }

    /***************************************************************************
     *                                                                         *
     * General convenience                                                     *
     *                                                                         *
     **************************************************************************/

    static <T> String getItemText(Cell<T> cell) {
        return cell.getItem() == null ? "" : cell.getItem().toString();
    }
    
    private static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
        return converter == null ?
            cell.getItem() == null ? "" : cell.getItem().toString() :
            converter.toString(cell.getItem());
    }


    
    /***************************************************************************
     *                                                                         *
     * Generic field convenience                                               *
     *                                                                         *
     **************************************************************************/

    static <T> void updateItem(final Cell<T> cell, final Node graphic) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                cell.setText(null);
                cell.setGraphic(graphic);
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
    
    static <T> void startEdit(final Cell<T> cell, final Node graphic) {
        cell.setText(null);
        cell.setGraphic(graphic);
    }

    static <T> void cancelEdit(Cell<T> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * TextField convenience                                                   *
     *                                                                         *
     **************************************************************************/

    static <T> void updateItem(final Cell<T> cell,
                               final StringConverter<T> converter,
                               final TextField textField) {
        updateItem(cell, converter, null, null, textField);
    }

    static <T> void updateItem(final Cell<T> cell,
                               final StringConverter<T> converter,
                               final HBox hbox,
                               final Node graphic,
                               final TextField textField) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText(cell, converter));
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, textField);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(textField);
                }
            } else {
                cell.setText(getItemText(cell, converter));
                cell.setGraphic(graphic);
            }
        }
    }

    static <T> void startEdit(final Cell<T> cell,
                              final StringConverter<T> converter,
                              final HBox hbox,
                              final Node graphic,
                              final TextField textField) {
        if (textField != null) {
            textField.setText(getItemText(cell, converter));
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, textField);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(textField);
        }

        textField.selectAll();

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        Platform.runLater(() -> textField.requestFocus());
    }
    
    static <T> void cancelEdit(Cell<T> cell, final StringConverter<T> converter, Node graphic) {
        cell.setText(getItemText(cell, converter));
        cell.setGraphic(graphic);
    }

    static <T> TextField createTextField(final Cell<T> cell, final StringConverter<T> converter) {
        final TextField textField = new TextField(getItemText(cell, converter));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction(event -> {
            if (converter == null) {
                throw new IllegalStateException(
                        "Attempting to convert text input into Object, but provided "
                                + "StringConverter is null. Be sure to set a StringConverter "
                                + "in your cell factory.");
            }
            cell.commitEdit(converter.fromString(textField.getText()));
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }
}
