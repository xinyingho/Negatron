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
package net.babelsoft.negatron.model.component;

import net.babelsoft.negatron.model.Option;

/**
 *
 * @author capan
 */
public class SlotOption extends Option<SlotOption> {
    protected static final SlotOption EMPTY_SLOT = new SlotOption();
    
    private SlotOption() {
        super();
    }
    
    protected SlotOption(SlotOption ref) {
        this(ref.getName(), ref.getDescription());
    }
    
    public SlotOption(String name, String description) {
        super(name, description);
    }
    
    @Override
    public String toString() {
        if (this != EMPTY_SLOT)
            return super.toString();
        else
            return "<empty>";
    }
    
    @Override
    public SlotOption copy() {
        return new SlotOption(this);
    }
}
