/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2024 BabelSoft S.A.S.U.
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

import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import net.babelsoft.negatron.scene.event.GamepadEvent;
import net.babelsoft.negatron.scene.input.GamepadButton;

/**
 *
 * @author Xiny
 */
public class TabPane extends javafx.scene.control.TabPane {
    public TabPane() {
        super();
        wireGamepadEvents();
    }
    
    private void wireGamepadEvents() {
        addEventHandler(GamepadEvent.GAMEPAD_BUTTON_CLICKED, event -> {
            SelectionModel<Tab> model = getSelectionModel();
            
            switch (event.getButton()) {
                case GamepadButton.LEFT_TRIGGER -> {
                    if (model.getSelectedIndex() != 0)
                        model.selectPrevious();
                    else
                        model.selectLast();
                    requestFocus();
                    event.consume();
                }
                case GamepadButton.RIGHT_TRIGGER -> {
                    if (model.getSelectedIndex() != (getTabs().size() - 1))
                        model.selectNext();
                    else
                        model.selectFirst();
                    requestFocus();
                    event.consume();
                }
            }
        });
    }
}
