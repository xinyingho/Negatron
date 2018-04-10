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
import javafx.fxml.FXMLLoader;
import net.babelsoft.negatron.controller.GlobalConfigurationPaneController;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Delegate;

/**
 *
 * @author capan
 */
public class GlobalConfigurationPane extends TitledWindowPane {
    
    private final GlobalConfigurationPaneController controller;
    
    public GlobalConfigurationPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/GlobalConfigurationPane.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void setOnRestart(Delegate delegate) {
        controller.setOnRestart(delegate);
    }

    public void selectOptionsTab() {
        controller.selectOptionsTab();
    }

    public void disableLanguageOption(boolean disable) {
        controller.disableLanguageOption(disable);
    }

    public void resetVlcPath() {
        controller.resetVlcPath();
    }
}
