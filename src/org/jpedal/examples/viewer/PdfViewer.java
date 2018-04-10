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

import java.util.ResourceBundle;
import javafx.scene.Parent;
import org.jpedal.PdfDecoderFX;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.javafx.JavaFXSearchWindow;
import org.jpedal.examples.viewer.gui.javafx.JavaFXThumbnailPanel;
import org.jpedal.examples.viewer.objects.ClientExternalHandler;
import org.jpedal.external.Options;
import org.jpedal.objects.acroforms.actions.JavaFXDefaultActionHandler;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 *
 * @author capan
 */
public class PdfViewer extends OpenViewerFX {

    public PdfViewer(Parent parentPane, String preferencesPath) {
        super(parentPane, preferencesPath);
    }
    
    // copy-pasted method from OpenViewerFX to wire the custom JavaFxGUI
    @Override
    void init() {
        
        //load locale file
        try {
            Messages.setBundle(ResourceBundle.getBundle("org.jpedal.international.messages"));
        } catch (final Exception e) {
            //
            LogWriter.writeLog("Exception " + e + " loading resource bundle.\n"
                    + "Also check you have a file in org.jpedal.international.messages to support Locale=" + java.util.Locale.getDefault());
        }
        
        //

        decode_pdf = new PdfDecoderFX();

        thumbnails = new JavaFXThumbnailPanel(decode_pdf);

        currentGUI = new PdfViewerGui(stage, decode_pdf, commonValues, thumbnails, properties);
        
        decode_pdf.addExternalHandler(new JavaFXDefaultActionHandler(currentGUI), Options.FormsActionHandler);
        decode_pdf.addExternalHandler(new ClientExternalHandler(), Options.AdditionalHandler);
        
        if(GUI.debugFX) {
            System.out.println("OpenViewerFX init()");
        }

        searchFrame = new JavaFXSearchWindow(currentGUI);

        currentCommands = new JavaFXCommands(commonValues, currentGUI, decode_pdf,
                thumbnails, properties, searchFrame, currentPrinter);
        
                
		//enable error messages which are OFF by default
		DecoderOptions.showErrorMessages=true;
		
		//
		
		
		final String prefFile = System.getProperty("org.jpedal.Viewer.Prefs");
		if(prefFile != null){
			properties.loadProperties(prefFile);
		}else{
			properties.loadProperties();
		}
    }
}
