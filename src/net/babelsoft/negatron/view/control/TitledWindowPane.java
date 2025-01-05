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

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.controller.TitledWindowPaneController;
import net.babelsoft.negatron.util.function.Delegate;

/**
 *
 * @author capan
 */
public class TitledWindowPane extends VBox {
    
    public static final String PROP_TEXT = "PROP_TEXT";
    public static final String PROP_GRAPHIC = "PROP_GRAPHIC";
    public static final String PROP_HEADER = "PROP_HEADER";
    public static final String PROP_CONTENT = "PROP_CONTENT";
    
    private String text;
    private ImageView graphic;
    private Node header;
    private Node content;
    private DisplayMode previousDisplayMode;
    private Delegate onceOnAnimationEnded;
    private boolean intermediateSize;
    private final ObjectProperty<DisplayMode> displayMode;
    private final TitledWindowPaneController controller;
    private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    public static enum DisplayMode {
        NONE,
        HIDDEN,
        INTERMEDIATE,
        MAXIMISED
    }
    
    public TitledWindowPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/TitledWindowPane.fxml"));
        fxmlLoader.setRoot(this);
        intermediateSize = true;
        
        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
            propertyChangeSupport.addPropertyChangeListener(evt -> {
                switch (evt.getPropertyName()) {
                    case PROP_TEXT -> controller.setText(text);
                    case PROP_GRAPHIC -> controller.setGraphic(graphic);
                    case PROP_HEADER -> controller.setHeader(header);
                    case PROP_CONTENT -> controller.setContent(content);
                }
            });
            previousDisplayMode = DisplayMode.HIDDEN;
            displayMode = new SimpleObjectProperty<>();
            displayMode.set(DisplayMode.HIDDEN);
            controller.setControl(this);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
        propertyChangeSupport.firePropertyChange(PROP_TEXT, null, text);
    }

    /**
     * @return the graphic
     */
    public ImageView getGraphic() {
        return graphic;
    }

    /**
     * @param graphic the graphic to set
     */
    public void setGraphic(ImageView graphic) {
        this.graphic = graphic;
        propertyChangeSupport.firePropertyChange(PROP_GRAPHIC, null, graphic);
    }
    
    public Node getHeader() {
        return header;
    }
    
    public void setHeader(Node header) {
        this.header = header;
        propertyChangeSupport.firePropertyChange(PROP_HEADER, null, header);
    }

    /**
     * @return the content
     */
    public Node getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Node content) {
        this.content = content;
        propertyChangeSupport.firePropertyChange(PROP_CONTENT, null, content);
    }
    
    public boolean isDisplayed() {
        return getDisplayMode() != DisplayMode.HIDDEN;
    }
    
    public boolean isHidden() {
        return getDisplayMode() == DisplayMode.HIDDEN;
    }

    /**
     * @return the displayMode
     */
    public DisplayMode getDisplayMode() {
        return displayMode.get();
    }

    /**
     * @param displayMode the displayMode to set
     */
    private void setDisplayMode(DisplayMode displayMode) {
        previousDisplayMode = getDisplayMode();
        this.displayMode.set(displayMode);
    }
    
    public ReadOnlyObjectProperty<DisplayMode> displayModeProperty() {
        return displayMode;
    }
    
    public DisplayMode getPreviousDisplayMode() {
        return previousDisplayMode;
    }
    
    public void setIntermediateSize(boolean value) {
        intermediateSize = value;
    }
    
    public boolean isIntermediateSize() {
        return intermediateSize;
    }
    
    public void setSyncWindow(TitledWindowPane window) {
        controller.setSyncWindow(this, window);
    }
    
    public void setWindowSlaves(TitledWindowPane... windows) {
        controller.setWindowSlaves(windows);
    }

    public void setOnClose(Delegate delegate) {
        controller.setOnClose(delegate);
    }
    
    public void setOnceOnAnimationEnded(Delegate delegate) {
        onceOnAnimationEnded = delegate;
    }
    
    public Delegate getOnceOnAnimationEnded() {
        return onceOnAnimationEnded;
    }
    
    public void hide() {
        setDisplayMode(DisplayMode.HIDDEN);
    }
    
    public void show() {
        setDisplayMode(DisplayMode.INTERMEDIATE);
    }
    
    public void showMaximised() {
        if (content instanceof TabPane)
            setOnceOnAnimationEnded(content::requestFocus);
        setDisplayMode(DisplayMode.MAXIMISED);
    }

    public void close() {
        controller.close();
    }
}
