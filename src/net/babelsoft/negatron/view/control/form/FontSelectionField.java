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

import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class FontSelectionField extends Field {
    
    private final HBox pane;
    private final ComboBox<String> fontBox;
    private final ComboBox<String> sizeBox;
    
    public FontSelectionField(GridPane grid, int row) {
        Label label = new Label(Language.Manager.getString("font"));
        grid.add(label, 0, row);
        
        boolean fontUpdated = false;
        
        fontBox = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontBox.setMaxWidth(Double.MAX_VALUE);
        fontBox.setTooltip(new Tooltip(Language.Manager.getString("font.tooltip")));
        fontBox.setCellFactory((listView) -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String family, boolean empty) {
                    super.updateItem(family, empty);

                    if (!empty) {
                        double size = sizeBox.getValue().isEmpty() ? -1.0 : Double.parseDouble(sizeBox.getValue());
                        setFont(Font.font(family, size));
                        setText(family);
                    } else {
                        setText(null);
                    }
                }
            };
        });
        String family = Configuration.Manager.getFontFamily();
        if (Strings.isEmpty(family))
            family = label.getFont().getFamily();
        else
            fontUpdated = true;
        fontBox.getSelectionModel().select(family);
        fontBox.setOnAction(evt -> onFontChanged(evt));
        
        sizeBox = new ComboBox<>();
        sizeBox.setEditable(true);
        sizeBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        sizeBox.setPromptText(Language.Manager.getString("font.size.label"));
        sizeBox.getItems().addAll(
            "8", "9", "10", "10.5", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"
        );
        double size = Configuration.Manager.getFontSize();
        if (size == 0)
            size = label.getFont().getSize();
        else
            fontUpdated = true;
        String sizeString = new DecimalFormat("0").format(size);
        sizeBox.getSelectionModel().select(sizeString);
        sizeBox.setOnAction(evt -> onFontChanged(evt));
        
        pane = new HBox(5.0);
        pane.getChildren().addAll(fontBox, sizeBox);
        HBox.setHgrow(fontBox, Priority.SOMETIMES);
        grid.add(pane, 1, row);
        
        if (fontUpdated) {
            String familyTmp = family; // workaround to be able to use "family" in the below lambda expression
            pane.sceneProperty().addListener(o -> Platform.runLater(() -> {
                if (pane.getScene() != null) // this test is needed only while changing GUI language as everything gets recreated
                    onFontChanged(familyTmp, sizeString);
            }));
        }
    }
    
    private void onFontChanged(String family, String size) {
        pane.getScene().getRoot().setStyle(String.format(
            "-fx-font-family: \"%s\"; -fx-font-size: %spx", family, size
        ));
    }
    private void onFontChanged(ActionEvent event) {
        String family = fontBox.getValue();
        String size = sizeBox.getValue();
        onFontChanged(family, size);
        updateFont(family, Double.parseDouble(size));
        
        if (event.getSource() == sizeBox) {
            // Force the font combobox to redraw the content of its popup list to the new font size
            fontBox.setOnAction(null);
            fontBox.getItems().clear();
            fontBox.setItems(FXCollections.observableArrayList(Font.getFamilies()));
            fontBox.getSelectionModel().select(family);
            fontBox.setOnAction(evt -> onFontChanged(evt));
        }
    }
}
