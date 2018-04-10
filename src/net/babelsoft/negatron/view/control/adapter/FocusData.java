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

import net.babelsoft.negatron.model.component.MachineComponent;

/**
 *
 * @author capan
 */
public class FocusData {
    
    private MachineComponent<?, ?> component;
    private final int caretPosition;
    
    public FocusData(MachineComponent<?, ?> component) {
        this(component, 0);
    }
    
    public FocusData(MachineComponent<?, ?> component, int caretPosition) {
        this.component = component;
        this.caretPosition = caretPosition;
    }
    
    public MachineComponent<?, ?> getComponent() {
        return component;
    }
    
    public int getCaretPosition() {
        return caretPosition;
    }
}
