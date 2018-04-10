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
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class MameIniField extends SinglePathField {
    
    private static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");
    
    public MameIniField(GridPane grid, int row, boolean isMess) {
        super(grid, row, "mameIni",
            isMess ? Language.Manager.getString("mameIni").replaceFirst("MAME", "MESS") : Language.Manager.getString("mameIni"),
            isMess ? Language.Manager.getString("mameIni.tooltip").replaceFirst("MAME", "MESS") : Language.Manager.getString("mameIni.tooltip")
        );
        
        String mameIni = Configuration.Manager.getMameIni();
        if (Strings.isValid(mameIni)) {
            pathField.setText(mameIni);
            pathField.pseudoClassStateChanged(ERROR_CLASS, false);
        } else
            pathField.pseudoClassStateChanged(ERROR_CLASS, true);
        
        pathField.textProperty().addListener((o, oV, newValue) -> {
            if (newValue.isEmpty())
                pathField.pseudoClassStateChanged(ERROR_CLASS, true);
            else
                pathField.pseudoClassStateChanged(ERROR_CLASS, false);
            
            updateMameIni(newValue);
        });
        
        browseButton.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            fc.getExtensionFilters().addAll(isMess ?
                new FileChooser.ExtensionFilter("mess.ini", "*mess*.ini")
                :
                new FileChooser.ExtensionFilter("mame.ini", "*mame*.ini"),
                new FileChooser.ExtensionFilter(Language.Manager.getString("allFiles"), "*")
            );
            File f = fc.showOpenDialog(browseButton.getScene().getWindow());
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
    }
}
