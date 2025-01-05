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
package net.babelsoft.negatron.scene.event;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import net.babelsoft.negatron.scene.input.GamepadButton;

/**
 *
 * @author Xiny
 */
public class GamepadEvent extends InputEvent {
    private static final long serialVersionUID = 1L;
    
    public static final EventType<GamepadEvent> GAMEPAD_BUTTON_CLICKED = new EventType<>(InputEvent.ANY, "GAMEPAD_BUTTON_CLICKED");
    
    public GamepadEvent(
        @NamedArg(value = "source") Object o,
        @NamedArg(value = "target") EventTarget et,
        @NamedArg(value = "button") GamepadButton button
    ) {
        super(o, et, GAMEPAD_BUTTON_CLICKED);
        this.button = button;
    }
    
    private final GamepadButton button;
    
    public final GamepadButton getButton() {
        return button;
    }
}
