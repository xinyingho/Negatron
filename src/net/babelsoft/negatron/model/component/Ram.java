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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.babelsoft.negatron.model.Option;

/**
 *
 * @author capan
 */
public class Ram extends Choice<RamOption> {
    private static final long serialVersionUID = 1L;
    
    public Ram() {
        super("ramsize");
    }
    
    protected Ram(Ram ref) {
        super(ref);
    }
    
    @Override
    public void setDefaultValue() {
        defaultValue = RamOption.DEFAULT_VALUE;
    }
    
    @Override
    public void addOption(RamOption option, boolean isDefault) {
        if (getOptions().isEmpty())
            super.addOption(RamOption.DEFAULT_VALUE, true);
        super.addOption(option, isDefault);
    }

    @Override
    public MachineElement<?> copy() {
        return new Ram(this);
    }
    
    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(getName());
        Option v = getValue();
        if (v != RamOption.DEFAULT_VALUE) {
            writer.writeAttribute("value", v.getName());
            if (v == getDefaultValue())
                writer.writeAttribute("default", Boolean.TRUE.toString());
        }
    }
}
