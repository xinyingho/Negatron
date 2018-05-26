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
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.TitledWindowPane;
import net.babelsoft.negatron.view.control.TitledWindowPane.DisplayMode;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class TitledWindowPaneController implements Initializable {
    
    @FXML
    private VBox root;
    @FXML
    private Label titleLabel;
    @FXML
    private HBox buttonBox;
    @FXML
    private Button minimiseButton;
    @FXML
    private Button restoreButton;
    @FXML
    private Button maximiseButton;
    @FXML
    private Button closeButton;
    @FXML
    private HBox headerPane;
    @FXML
    private StackPane contentPane;
    
    private TitledWindowPane control;
    private TitledWindowPane[] windowSlaves;
    private TitledWindowPane syncWindow;
    private Delegate onClose;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        minimiseButton.setOnAction(evt -> {
            control.hide();
            if (syncWindow != null)
                syncWindow.showMaximised();
        });
        restoreButton.setOnAction(evt -> {
            control.show();
            if (syncWindow != null)
                syncWindow.show();
        });
        maximiseButton.setOnAction(evt -> {
            control.showMaximised();
            if (syncWindow != null)
                syncWindow.hide();
        });
        closeButton.setOnAction(evt -> {
            if (onClose != null)
                onClose.fire();
            control.hide();
            if (windowSlaves != null)
                Arrays.stream(windowSlaves).forEach(
                    window -> window.hide()
                );
            if (syncWindow != null) {
                syncWindow.setSyncWindow(null);
                syncWindow = null;
            }
        });
    }
    
    private void layoutButtonBox() {
        ObservableList<Node> children = buttonBox.getChildren();
        children.clear();
        
        if (syncWindow != null)
            children.add(minimiseButton);
        
        if (control.isIntermediateSize()) switch (control.getDisplayMode()) {
            case INTERMEDIATE:
                children.addAll(maximiseButton, closeButton);
                break;
            case MAXIMISED:
                children.addAll(restoreButton, closeButton);
                break;
        } else
            children.addAll(closeButton);
    }

    private boolean isEscPressed = false;
    public void setControl(TitledWindowPane control) {
        this.control = control;
        control.displayModeProperty().addListener((o, oV, newValue) -> {
            if (newValue != DisplayMode.HIDDEN)
                layoutButtonBox();
        });
        
        // Workaround for an unwanted chain of events. Normally:
        // 1- the user presses the Escape key while MAME is running.
        // 2- the Escape pressed event is caught by MAME and then close.
        // 3- Negatron gets the focus back.
        // 4- the Escape released event is raised, thus Negatron eventually closes any opened Software related panes.
        // So the below workaround allows to close opened Software related panes only when the complete Escape event cycle (pressed/typed/released) has been done from Negatron.
        control.addEventFilter(KeyEvent.KEY_PRESSED, event -> { // TODO find what blocks and cancels addEventHandler(KEY_PRESSED) from the Software Tree Table
            if (event.getCode() == KeyCode.ESCAPE)
                isEscPressed = true;
        });
        control.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && isEscPressed) {
                isEscPressed = false;
                closeButton.fire();
            }
        });
    }

    public void setText(String text) {
        titleLabel.setText(text);
    }

    public void setGraphic(ImageView graphic) {
        titleLabel.setGraphic(graphic);
    }
    
    public void setHeader(Node header) {
        headerPane.getChildren().add(header);
    }

    public void setContent(Node content) {
        if (content != null)
            contentPane.getChildren().setAll(content);
        else
            contentPane.getChildren().clear();
    }

    public void setWindowSlaves(TitledWindowPane[] windows) {
        windowSlaves = windows;
    }

    public void setSyncWindow(TitledWindowPane reference, TitledWindowPane window) {
        if (syncWindow != window) {
            syncWindow = window;
            layoutButtonBox();
            
            if (window != null)
                window.setSyncWindow(reference);
        }
    }

    public void setOnClose(Delegate delegate) {
        onClose = delegate;
    }

    public void close() {
        closeButton.fire();
    }
}
