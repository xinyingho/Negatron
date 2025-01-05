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

/**
 *
 * @author Xiny
 */
public class JoystickEvent extends InputEvent {
    private static final long serialVersionUID = 1L;
    
    public static final EventType<JoystickEvent> ANY = new EventType<>(InputEvent.ANY, "JOYSTICK");
    public static final EventType<JoystickEvent> JOYSTICK_ADDED = new EventType<>(JoystickEvent.ANY, "JOYSTICK_ADDED");
    public static final EventType<JoystickEvent> JOYSTICK_ADDED_ERROR = new EventType<>(JoystickEvent.ANY, "JOYSTICK_ADDED_ERROR");
    public static final EventType<JoystickEvent> JOYSTICK_REMOVED = new EventType<>(JoystickEvent.ANY, "JOYSTICK_REMOVED");
    public static final EventType<JoystickEvent> JOYSTICK_GAMEPAD = new EventType<>(JoystickEvent.ANY, "JOYSTICK_GAMEPAD");
    public static final EventType<JoystickEvent> JOYSTICK_GAMEPAD_ERROR = new EventType<>(JoystickEvent.ANY, "JOYSTICK_GAMEPAD_ERROR");

    public JoystickEvent(
        @NamedArg(value = "source") Object o,
        @NamedArg(value = "target") EventTarget et,
        @NamedArg(value = "eventType") EventType<JoystickEvent> et1,
        @NamedArg(value = "joystickName") String name,
        @NamedArg(value = "additionalInformation") String info
    ) {
        super(o, et, et1);
        this.name = name;
        this.info = info;
    }
    
    private final String name;
    
    public final String getName() {
        return name;
    }
    
    private final String info;
    
    public final String getInfo() {
        return info;
    }
}
