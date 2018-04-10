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
package org.jpedal.examples.viewer;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.examples.viewer.gui.javafx.JavaFXMenuItems;
import org.jpedal.examples.viewer.utils.PropertiesFile;

/**
 *
 * @author capan
 */
public class PdfViewerGui extends JavaFxGUI {

    public PdfViewerGui(Stage stage, PdfDecoderInt decode_pdf, Values commonValues, GUIThumbnailPanel thumbnails, PropertiesFile properties) {
        super(stage, decode_pdf, commonValues, thumbnails, properties);
    }
    
    @Override
    public void init(final Commands currentCommands, final Object currentPrinter) {
        super.init(currentCommands, currentPrinter);
        
        // remove everything's not needed
        
        topPane.getChildren().remove(((JavaFXMenuItems) menuItems).getCurrentMenuFX());
                
        ObservableList<Node> nodes = fxButtons.getTopButtons().getItems();
        nodes.remove(0); // open files
        nodes.remove(0); // print
        nodes.remove(0); // separator
        nodes.remove(0); // screenshot
        
        coordsFX = null;
        downloadBar = null;
        memoryBarFX = null;
        multiboxfx.getChildren().clear();
        navButtons.getChildren().remove(multiboxfx);
        multiboxfx = null;
        memoryMonitor.stop();
        memoryMonitor = null;
        
        navButtons.getChildren().remove(pagesToolBar);
        pagesToolBar.getItems().clear();
        pagesToolBar = null;
    }
    
    @Override
    public void enableCursor(final boolean enabled, final boolean visible) { }

    @Override
    public void enableMemoryBar(final boolean enabled, final boolean visible) { }

    @Override
    public void enableDownloadBar(final boolean enabled, final boolean visible) { }
    
    @Override
    public void setMultibox(final int[] flags) { }
    
    @Override
    public void setCoordText(final String string) { }
}
