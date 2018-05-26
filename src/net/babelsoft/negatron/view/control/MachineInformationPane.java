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
package net.babelsoft.negatron.view.control;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import net.babelsoft.negatron.controller.MachineInformationPaneController;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.util.Disposable;

/**
 *
 * @author capan
 */
public class MachineInformationPane extends VBox implements Disposable {
    
    private final MachineInformationPaneController controller;
    
    public MachineInformationPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/MachineInformationPane.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public TabPane getTabPane() {
        return controller.getTabPane();
    }
    
    public void setEmulatedItem(Machine machine) {
        controller.setEmulatedItem(machine);
    }

    public void setApplication(Application application) {
        controller.setApplication(application);
    }
    
    public void selectInformationTab() {
        controller.selectInformationTab();
    }
    
    public void showTab() {
        controller.showTab();
    }
    
    public void hideTab(Delegate onStarted) {
        controller.hideTab(onStarted, true);
    }
    
    public void hideTab(EventHandler<ActionEvent> onFinished) {
        controller.hideTab(onFinished);
    }

    @Override
    public void dispose() {
        controller.dispose();
    }
    
    public void setFavouriteEnabled(boolean favouriteEnabled) {
        controller.setFavouriteEnabled(favouriteEnabled);
    }

    public void setSoundEnabled(boolean soundEnabled) {
        controller.setSoundEnabled(soundEnabled);
    }
    
    public void setVideoEnabled(boolean videoEnabled) {
        controller.setVideoEnabled(videoEnabled);
    }

    public void pauseVideo() {
        controller.pauseVideo();
    }
    
    public void setOnVideoShortcut(Delegate delegate) {
        controller.setOnVideoShortcut(delegate);
    }
}
