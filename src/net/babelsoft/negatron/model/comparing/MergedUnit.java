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
package net.babelsoft.negatron.model.comparing;

import net.babelsoft.negatron.model.component.MachineElement;

/**
 *
 * @author capan
 */
public class MergedUnit<T> {
    
    private final MachineElement<T> oldElement;
    private final MachineElement<T> newElement;
    private final Difference status;
    
    public MergedUnit(MachineElement<T> oldElement, MachineElement<T> newElement, Difference status) {
        this.oldElement = oldElement;
        this.newElement = newElement;
        this.status = status;
    }

    /**
     * @return the oldElement
     */
    public MachineElement<T> getOldElement() {
        return oldElement;
    }

    /**
     * @return the newElement
     */
    public MachineElement<T> getNewElement() {
        return newElement;
    }

    /**
     * @return the status
     */
    public Difference getStatus() {
        return status;
    }
}
