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

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import net.babelsoft.negatron.io.configuration.Domain;

/**
 *
 * @author capan
 */
public class GridAdornment {

    private static final Image MACHINE_IMG = new Image(MultiPathField.class.getResourceAsStream(
        "/net/babelsoft/negatron/resource/icon/machine.png"
    ));
    private static final Image SOFTWARE_IMG = new Image(MultiPathField.class.getResourceAsStream(
        "/net/babelsoft/negatron/resource/icon/software.png"
    ));
    
    public static void insertSpacing(GridPane grid, int row, double spacing) {
        RowConstraints constraints = new RowConstraints();
        constraints.setMinHeight(spacing);
        grid.getRowConstraints().add(constraints);
    }
    
    public static void insertTitle(GridPane grid, int row, double height, String text) {
        RowConstraints constraints = new RowConstraints();
        constraints.setMinHeight(height);
        grid.getRowConstraints().add(constraints);
        
        Label label = new Label(text);
        grid.add(label, 0, row, 3, 1);
        GridPane.setHalignment(label, HPos.CENTER);
    }
    
    public static void insertHeader(GridPane grid, int row, double minHeight, Domain style) {
        HBox box = new HBox(10.0);
        box.setPadding(new Insets(0, 5.0, 0, 0));
        box.setAlignment(Pos.CENTER_RIGHT);

        RowConstraints constraints = new RowConstraints();
        constraints.setMinHeight(minHeight);
        grid.getRowConstraints().add(constraints);
        grid.add(box, 0, row, 3, 1);

        switch (style) {
            case EXTRAS_MACHINE_SOFTWARE:
            case MULTIMEDIA_MACHINE_SOFTWARE:
                box.getChildren().add(new ImageView(MACHINE_IMG));
                box.getChildren().add(new ImageView(SOFTWARE_IMG));
                break;
            case EXTRAS_MACHINE_ONLY:
            case MULTIMEDIA_MACHINE_ONLY:
                box.getChildren().add(new ImageView(MACHINE_IMG));
                break;
            case EXTRAS_SOFTWARE_ONLY:
                box.getChildren().add(new ImageView(SOFTWARE_IMG));
                break;
        }
    }
}
