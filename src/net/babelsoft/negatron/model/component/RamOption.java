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
import net.babelsoft.negatron.model.SizeUnit;

/**
 *
 * @author capan
 */
public class RamOption extends Option<RamOption> {
    protected static final RamOption DEFAULT_VALUE = new RamOption();
    
    private RamOption() {
        super();
    }
    
    public RamOption(String name) {
        super(name);
        long value = Long.parseUnsignedLong(name);
        setDescription(SizeUnit.factorise(value));
    }
    
    protected RamOption(RamOption ref) {
        this(ref.getName());
    }
    
    @Override
    public String toString() {
        if (this != DEFAULT_VALUE)
            return super.toString();
        else
            return "<default>";
    }
    
    @Override
    public RamOption copy() {
        return new RamOption(this);
    }
}
