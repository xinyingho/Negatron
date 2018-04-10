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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.theme.Skin;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class SkinChoiceField extends ChoiceField<Skin> {
    
    private static final Skin DEFAULT_SKIN;
    static {
        DEFAULT_SKIN = new Skin("modena", "Modena (default)", "");
    }
    
    public SkinChoiceField(GridPane grid, int row) {
        super(grid, row, Language.Manager.getString("skin"), Language.Manager.getString("skin.tooltip"));
        
        choiceBox.getItems().add(DEFAULT_SKIN);
        
        Path path = Paths.get("theme/skin");
        if (Files.exists(path)) try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.endsWith("manifest.mf")) {
                        Path skinFile = file.getParent().resolve(Paths.get("skin.css"));
                        if (Files.exists(skinFile)) {
                            // unoptimised reading
                            String name = Files.lines(file).filter(
                                line -> line.startsWith("Specification-Title:")
                            ).findAny().map(
                                line -> line.substring(20).trim()
                            ).orElse(null);
                            
                            String description = Files.lines(file).filter(
                                line -> line.startsWith("Implementation-Title:")
                            ).findAny().map(
                                line -> line.substring(21).trim()
                            ).orElse(null);
                            
                            if (name != null && description != null)
                                choiceBox.getItems().add(new Skin(
                                    name, description, skinFile
                                ));
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
        
        choiceBox.sceneProperty().addListener((o, oV, newValue) -> {
            if (newValue == null)
                return;
            
            String skinName = Configuration.Manager.getSkin();
            if (Strings.isValid(skinName))
                choiceBox.getSelectionModel().select(
                    choiceBox.getItems().stream().filter(
                        skin -> skinName.equals(skin.getName())
                    ).findAny().orElse(DEFAULT_SKIN)
                );
            else
                choiceBox.getSelectionModel().selectFirst();
            onAction();
        
            choiceBox.setOnAction(event -> {
                Skin skin = onAction();
                if (skin != null)
                    updateSkin(skin.getName());
                event.consume();
            });
        });
    }
    
    private Skin onAction() {
        Skin skin = choiceBox.getSelectionModel().getSelectedItem();
        if (skin != null) {
            if (Strings.isValid(skin.getCss()))
                choiceBox.getScene().getStylesheets().setAll(skin.getCss());
            else
                choiceBox.getScene().getStylesheets().clear();
        }
        return skin;
    }
}
