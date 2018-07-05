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
import net.babelsoft.negatron.controller.NotifierPopupController;
import net.babelsoft.negatron.io.loader.LoadingObserver;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class NotifierPopup extends PopOver implements LoadingObserver {
    
    private NotifierPopupController controller;
    
    public NotifierPopup() {
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/net/babelsoft/negatron/view/control/fxml/NotifierPopup.fxml"), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void begin(String id, int total) {
        controller.begin(id, total);
    }

    @Override
    public void notify(String id, int processed) {
        controller.notify(id, processed);
    }

    @Override
    public void end(String id) {
        controller.end(id);
    }
}
