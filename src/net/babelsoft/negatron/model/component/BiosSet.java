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
import static net.babelsoft.negatron.model.component.RamOption.DEFAULT_VALUE;

/**
 *
 * @author capan
 */
public class BiosSet extends Option<BiosSet> {
    protected static final BiosSet DEFAULT_VALUE = new BiosSet();
    
    private BiosSet() {
        super();
    }
    
    protected BiosSet(BiosSet ref) {
        this(ref.getName(), ref.getDescription());
    }
    
    public BiosSet(String name, String description) {
        super(name, description);
    }
    
    @Override
    public String toString() {
        if (this != DEFAULT_VALUE)
            return super.toString();
        else
            return "<default>";
    }
    
    @Override
    public BiosSet copy() {
        return new BiosSet(this);
    }
}
