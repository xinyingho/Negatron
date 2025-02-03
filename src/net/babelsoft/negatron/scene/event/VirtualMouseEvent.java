/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2025 BabelSoft S.A.S.U.
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
 * @author xinyingho
 */
public class VirtualMouseEvent extends InputEvent {
    private static final long serialVersionUID = 1L;
    
    public static final EventType<VirtualMouseEvent> ANY = new EventType<>(InputEvent.ANY, "VMOUSE");
    public static final EventType<VirtualMouseEvent> VMOUSE_ERROR = new EventType<>(VirtualMouseEvent.ANY, "VMOUSE_ERROR");
    public static final EventType<VirtualMouseEvent> VMOUSE_WARNING = new EventType<>(VirtualMouseEvent.ANY, "VMOUSE_WARNING");

    public VirtualMouseEvent(
        @NamedArg(value = "source") Object o,
        @NamedArg(value = "target") EventTarget et,
        @NamedArg(value = "eventType") EventType<VirtualMouseEvent> et1,
        @NamedArg(value = "additionalInformation") String info
    ) {
        super(o, et, et1);
        this.info = info;
    }
    
    private final String info;
    
    public final String getInfo() {
        return info;
    }
}
