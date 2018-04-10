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

import java.io.File;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Shell;

/**
 *
 * @author capan
 */
public class VlcPathField extends SinglePathField {
    
    boolean canUpdatePath;
    
    public VlcPathField(GridPane grid, int row) {
        super(grid, row, "VLC", Language.Manager.getString("vlc.tooltip"));
        
        canUpdatePath = true;
        pathField.setText(Configuration.Manager.getVlcPath());
        pathField.textProperty().addListener((o, oV, newValue) -> {
            if (canUpdatePath)
                updateVlcPath(newValue);
        });
        
        browseButton.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            if (Shell.isWindows())
                fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("libVLC", "libvlc.dll")
                );
            else // Mac OS X or Linux
                fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("libVLC", "libvlc.so")
                );
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(Language.Manager.getString("allFiles"), "*")
            );
            File f = fc.showOpenDialog(browseButton.getScene().getWindow());
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
    }

    public void reset() {
        canUpdatePath = false;
        pathField.setText(Configuration.Manager.getVlcPath());
        canUpdatePath = true;
    }
}
