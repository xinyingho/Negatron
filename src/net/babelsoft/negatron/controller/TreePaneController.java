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
package net.babelsoft.negatron.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.TreeTableColumnConfiguration;
import net.babelsoft.negatron.view.control.NegatronTreeView;
import net.babelsoft.negatron.view.control.adapter.TableColumnsAdapter;

/**
 *
 * @author capan
 */
public abstract class TreePaneController<T extends NegatronTreeView<I>, I> implements Initializable {
    
    @FXML
    protected T treeView;
    
    protected String id;
    private TableColumnsAdapter<TreeTableColumn<I, ?>, TreeTableColumnConfiguration> columnsAdapter;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        columnsAdapter = new TableColumnsAdapter<>(
            treeView.getColumns(), treeView.getSortOrder(),
            (col, conf) -> col.setSortType(conf.getSortType()),
            (conf, col) -> conf.setSortType(col.getSortType()),
            TreeTableColumnConfiguration.class,
            conf -> {
                try {
                    Configuration.Manager.updateTreeTableColumnsConfiguration(id, conf);
                } catch (IOException ex) {
                    Logger.getLogger(TreePaneController.class.getName()).log(Level.SEVERE, "Tree table column layout configuration couldn't be saved", ex);
                }
            }
        );
        
        treeView.initialize(url, rb);
    }
    
    public void setId(String id) {
        this.id = id;
        loadLayout();
    }
    
    protected void loadLayout() {
        columnsAdapter.loadLayout(
            Configuration.Manager.getTreeTableColumnsConfiguration(id)
        );
        
        final Timeline resizeTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> saveColumnsLayout())
        );
        treeView.setOnSort(evt -> saveColumnsLayout());
        treeView.getColumns().addListener((ListChangeListener.Change<? extends TreeTableColumn<I, ?>> c) -> {
            while (c.next())
                if (c.wasReplaced() || c.wasPermutated() || c.wasUpdated()) {
                    saveColumnsLayout();
                    break;
                }
        });
        treeView.getColumns().forEach(col -> {
            col.widthProperty().addListener((o, oV, nV) -> resizeTimeline.playFromStart());
            col.visibleProperty().addListener((o, oV, nV) -> saveColumnsLayout());
        });
    }
    
    private void saveColumnsLayout() {
        columnsAdapter.saveColumnsLayout(id);
    }
}
