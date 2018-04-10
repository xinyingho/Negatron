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
import javafx.css.PseudoClass;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Shell;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class MamePathField extends SinglePathField {
    
    private static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");
    
    public MamePathField(GridPane grid, int row, boolean isMess) {
        super(grid, row, "mameExe",
            isMess ? Language.Manager.getString("mameExe").replaceFirst("MAME", "MESS") : Language.Manager.getString("mameExe"),
            isMess ? Language.Manager.getString("mameExe.tooltip").replaceFirst("MAME", "MESS") : Language.Manager.getString("mameExe.tooltip")
        );
        
        String mamePath = Configuration.Manager.getMameExecutable();
        if (Strings.isValid(mamePath)) {
            pathField.setText(mamePath);
            pathField.pseudoClassStateChanged(ERROR_CLASS, false);
        } else
            pathField.pseudoClassStateChanged(ERROR_CLASS, true);
        
        pathField.textProperty().addListener((o, oV, newValue) -> {
            if (newValue.isEmpty())
                pathField.pseudoClassStateChanged(ERROR_CLASS, true);
            else
                pathField.pseudoClassStateChanged(ERROR_CLASS, false);
            
            updateMamePath(newValue);
        });
        
        browseButton.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            if (Shell.isWindows())
                fc.getExtensionFilters().add(
                    isMess ?
                    new FileChooser.ExtensionFilter("MESS", "*mess*.exe")
                    :
                    new FileChooser.ExtensionFilter("MAME", "*mame*.exe")
                );
            else // Mac OS X or Linux
                fc.getExtensionFilters().add(
                    isMess ?
                    new FileChooser.ExtensionFilter("MESS", "*mess*")
                    :
                    new FileChooser.ExtensionFilter("MAME", "*mame*")
                );
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(Language.Manager.getString("allFiles"), "*")
            );
            File f = fc.showOpenDialog(browseButton.getScene().getWindow());
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
    }
}
