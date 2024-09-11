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

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Shell;

/**
 *
 * @author capan
 */
public class AdvancedParametrisationDialog extends Dialog<String> {
    
    private final GridPane grid;
    private final Label label;
    private final TextArea textArea;
    private final String defaultValue;
    
    /**
     * Creates a Label node that works well within a Dialog.
     * @param text The text to display
     */
    static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }
    
    public AdvancedParametrisationDialog(Window owner, String params) {
        this(owner, "<video>", params, ButtonType.OK);
    }
    
    public AdvancedParametrisationDialog(Window owner, String name, String params) {
        this(owner, name, params, ButtonType.OK);
    }
    
    public AdvancedParametrisationDialog(Window owner, String name, String params, ButtonType okButtonType) {
        //// TextInputDialog code
        
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        textArea = new TextArea(params);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setWrapText(true);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);
        GridPane.setFillHeight(textArea, true);

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.setAlignment(Pos.TOP_LEFT);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.defaultValue = params;

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(Language.Manager.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(Language.Manager.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? textArea.getText() : null;
        });
        
        //// Additional code
        
        boolean isMess = Configuration.Manager.isMess();
        ResourceBundle language = Language.Manager.getBundle();
        
        setResizable(true);
        setTitle(language.getString("advancedParametrisation"));
        setHeaderText(isMess ? language.getString("advancedParametrisation.text").replaceFirst("MAME", "MESS") : language.getString("advancedParametrisation.text"));
        if (Shell.isWindows())
            setContentText((isMess ? "mess" : "mame") + ".exe");
        else // Mac OS X or Linux
            setContentText(isMess ? "mess" : "mame");
        setGraphic(new ImageView(getClass().getResource(
            "/net/babelsoft/negatron/resource/icon/" + (isMess ? "MESS" : "MAME") + ".png"
        ).toExternalForm()));
        getDialogPane().getButtonTypes().setAll(okButtonType, ButtonType.CANCEL);

        Button noDrcButton = new Button(language.getString("noDynaRec"));
        noDrcButton.setTooltip(new Infotip(language.getString("noDynaRec.tooltip")));
        noDrcButton.setOnAction(evt -> getEditor().appendText(" -nodrc"));
        Button biosButton = new Button(language.getString("bios"));
        biosButton.setTooltip(new Infotip(language.getString("bios.tooltip")));
        biosButton.setOnAction(evt -> getEditor().appendText(
            String.format(" -bios <%s>", language.getString("bios.parameter"))
        ));
        Button ramSizeButton = new Button(language.getString("ramSize"));
        ramSizeButton.setTooltip(new Infotip(language.getString("ramSize.tooltip")));
        ramSizeButton.setOnAction(evt -> getEditor().appendText(
            String.format(" -ramsize <%s>", language.getString("ramSize.parameter"))
        ));
        Button videoCaptureButton = new Button("videoCapture");
        //videoCaptureButton.setTooltip(new Infotip(language.getString("videoCapture.tooltip")));
        videoCaptureButton.setOnAction(evt -> getEditor().appendText(
            String.format(" -snapview native -aviwrite %s.avi", name)
        ));
        Button snapSizeButton = new Button("snapSize");
        //snapSizeButton.setTooltip(new Infotip(language.getString("snapSize.tooltip")));
        snapSizeButton.setOnAction(evt -> getEditor().appendText(
            String.format(" -snapsize 320x224")
        ));
        
        HBox box = new HBox();
        box.setSpacing(5);
        box.getChildren().add(noDrcButton);
        box.getChildren().add(biosButton);
        box.getChildren().add(ramSizeButton);
        box.getChildren().add(videoCaptureButton);
        box.getChildren().add(snapSizeButton);
        GridPane content = (GridPane) getDialogPane().getContent();
        content.setVgap(5);
        content.add(box, 1, 1);
        
        initOwner(owner);
    }
    
    /**
     * Returns the {@link TextArea} used within this dialog.
     */
    public final TextArea getEditor() {
        return textArea;
    }

    /**
     * Returns the default value that was specified in the constructor.
     */
    public final String getDefaultValue() {
        return defaultValue;
    }
    
    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(textArea, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> textArea.requestFocus());
    }
}
