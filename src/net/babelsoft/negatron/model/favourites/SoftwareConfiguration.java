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
package net.babelsoft.negatron.model.favourites;

import java.io.Serializable;
import net.babelsoft.negatron.model.Describable;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwarePart;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class SoftwareConfiguration implements Describable, Serializable {
    private static final long serialVersionUID = 1L;
    
    private String machine;
    private String device;
    private Software software;
    private SoftwarePart softwarePart;
    
    public SoftwareConfiguration(Machine machine, Device device, Software software) {
        this.machine = machine.getName();
        this.device = device.getName();
        this.software = software;
        
        String deviceValue = device.getValue();
        if (deviceValue.contains(":")) {
            String softwarePartName = deviceValue.split(":")[1];
            softwarePart = software.getSoftwareParts().stream().filter(
                part -> part.getName().equals(softwarePartName)
            ).findAny().orElse(null);
        } else
            softwarePart = null;
    }
    
    public String getMachine() {
        return machine;
    }

    public String getDevice() {
        return device;
    }

    public Software getSoftware() {
        return software;
    }
    
    public SoftwarePart getSoftwarePart() {
        return softwarePart;
    }

    @Override
    public String getDescription() {
        return software.getDescription();
    }
    
    @Override
    public String toString() {
        return String.format("-%s %s%s", device, software.getName(), Strings.orElseBlank(softwarePart, ":" + softwarePart.getName()));
    }
}
