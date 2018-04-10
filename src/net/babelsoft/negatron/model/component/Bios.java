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
public class Bios extends Choice<BiosSet> {
    
    public Bios() {
        super("bios");
    }
    
    protected Bios(Bios ref) {
        super(ref);
    }
    
    @Override
    public void setDefaultValue() {
        // This method's body should be empty as by default MAME explicitly select the 1st available bios
        // But it's still required for Negatron 0.98 and older where it could mistakenly record favourites
        // with empty bios values instead of forcing the 1st available bios
        defaultValue = BiosSet.DEFAULT_VALUE;
    }
    
    @Override
    public void addOption(BiosSet option, boolean isDefault) {
        if (getOptions().isEmpty())
            super.addOption(option, true);
        else
            super.addOption(option, isDefault);
    }

    @Override
    public MachineElement<?> copy() {
        return new Bios(this);
    }
    
    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(getName());
        Option v = getValue();
        if (v != BiosSet.DEFAULT_VALUE) {
            writer.writeAttribute("name", v.getName());
            writer.writeAttribute("description", v.getDescription());
            if (v == getDefaultValue())
                writer.writeAttribute("default", Boolean.TRUE.toString());
        }
    }
}
