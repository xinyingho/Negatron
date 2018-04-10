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
public class ChdmanPathField extends SinglePathField {
    
    public ChdmanPathField(GridPane grid, int row) {
        super(grid, row, "chdman", "CHDMAN", Language.Manager.getString("chdman.tooltip"));
        
        pathField.setText(Configuration.Manager.getChdmanExecutable());
        pathField.textProperty().addListener((o, oV, newValue) -> updateChdmanPath(newValue));
        
        browseButton.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            if (Shell.isWindows())
                fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CHDMAN", "chdman.exe")
                );
            else // Mac OS X or Linux
                fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CHDMAN", "chdman")
                );
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(Language.Manager.getString("allFiles"), "*")
            );
            File f = fc.showOpenDialog(browseButton.getScene().getWindow());
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
    }
}
