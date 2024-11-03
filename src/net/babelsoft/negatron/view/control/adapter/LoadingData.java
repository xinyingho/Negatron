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
package net.babelsoft.negatron.view.control.adapter;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class LoadingData {
    
    private final ProgressBar bar;
    private final AnchorPane iconPane;
    private final double totalCount;
    private int processedTotalCount;

    public LoadingData(GridPane grid, String id, int rowIndex, int totalCount) {
        this.totalCount = totalCount;
        bar = new ProgressBar(totalCount > 0 ? 0.0 : -1.0);
        bar.setPrefWidth(200.0);
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefHeight(20.0);
        indicator.setPrefWidth(20.0);
        iconPane = new AnchorPane(indicator);
        iconPane.setPrefHeight(20.0);
        iconPane.setPrefWidth(20.0);

        Platform.runLater(() -> {
            grid.add(new Label(Language.Manager.getString(id)), 0, rowIndex);
            grid.add(bar, 1, rowIndex);
            grid.add(iconPane, 2, rowIndex);
        });
    }

    public void incrementProcessed(int processed) {
        processedTotalCount += processed;
        bar.setProgress(processedTotalCount / totalCount);
    }

    public void end() {
        bar.setProgress(1.0);
        Platform.runLater(() -> iconPane.getChildren().set(
            0, new ImageView("/net/babelsoft/negatron/resource/icon/status/dialog-ok-apply.png")
        ));
    }
}
