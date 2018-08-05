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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class MameLanguageChoiceField extends ComboField<String> {
    
    public MameLanguageChoiceField(GridPane grid, int row) {
        super(grid, row, Language.Manager.getString("language"), Language.Manager.getString("language.tooltip"));
        
        String mamePath = Configuration.Manager.getMamePath();
        String languagePaths = Configuration.Manager.getGlobalConfiguration("languagepath");
        
        if (Strings.isValid(languagePaths))
            Arrays.asList(languagePaths.split(";")).forEach(rootPath -> {
                Path root = !Paths.get(rootPath).isAbsolute() ?
                    Paths.get(mamePath).resolve(rootPath) : Paths.get(rootPath)
                ;
                try {
                    Files.walk(root, 1).filter(
                        language -> language != root && language.toFile().isDirectory()
                    ).forEach(
                        language -> comboBox.getItems().add(language.getFileName().toString())
                    );
                } catch(IOException ex) { } // swallow exceptions
            });
        if (comboBox.getItems().size() > 0) {
            comboBox.setConverter(new StringConverter<String>() {
                @Override
                public String toString(String t) {
                    return t.replace("_", " ");
                }

                @Override
                public String fromString(String string) { // never called
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            comboBox.getSelectionModel().select(
                Configuration.Manager.getGlobalConfiguration("language")
            );
            comboBox.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
                try {
                    Configuration.Manager.updateGlobalConfigurationSetting("language", newValue);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(MameLanguageChoiceField.class.getName()).log(Level.SEVERE, "Couldn't save requested language configuration to MAME", ex);
                }
            });
        }
    }
}
