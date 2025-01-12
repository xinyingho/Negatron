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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.PathUtil;
import net.babelsoft.negatron.util.PathUtil.PathType;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.util.function.Delegate;

/**
 *
 * @author capan
 */
public class NegatronLanguageChoiceField extends ChoiceField<Locale> {
    
    Delegate onRestart;
    
    public NegatronLanguageChoiceField(GridPane grid, int row) {
        super(grid, row, Language.Manager.getString("language"), Language.Manager.getString("language.tooltip"));
        
        choiceBox.getItems().add(Locale.UK);
        
        Path path = PathUtil.retrieveFromJavaLibraryPaths(PathType.FOLDER, Language.Manager.ROOT_PATH);
        if (path != null) try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filename = file.getFileName().toString();
                    if (filename.matches(Language.Manager.MASK)) {
                        int i = filename.indexOf('_');
                        if (i >= 0) {
                            String locale = filename.substring(i + 1, filename.length() - 11).replace('_', '-');
                            choiceBox.getItems().add(Locale.forLanguageTag(locale));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null)
                        return FileVisitResult.CONTINUE;
                    else
                        throw e; // directory iteration failed
                }
            });
        } catch (IOException ex) { } // swallow exceptions
        
        choiceBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                return locale.getDisplayName(locale);
            }

            @Override
            public Locale fromString(String string) { // should never be called
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        
        String language = Configuration.Manager.getLanguage();
        Locale locale;
        if (Strings.isEmpty(language)) {
            locale = Locale.getDefault();
            if (!choiceBox.getItems().contains(locale))
                locale = Locale.UK;
        } else
            locale = Locale.forLanguageTag(language);
        choiceBox.getSelectionModel().select(locale);
        
        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
            try {
                Configuration.Manager.updateLanguage(newValue.toLanguageTag());
            } catch (IOException ex) {
                Logger.getLogger(NegatronLanguageChoiceField.class.getName()).log(Level.SEVERE, "Couldn't save requested language configuration to Negatron", ex);
            }
            Locale.setDefault(newValue);
            onRestart.fire();
        });
    }
    
    public void setOnRestart(Delegate delegate) {
        onRestart = delegate;
    }
}
