/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2020 BabelSoft S.A.S.U.
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

import java.util.concurrent.atomic.AtomicInteger;
import net.babelsoft.negatron.controller.DeviceController;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;

/**
 *
 * @author capan
 */
public class SelectionData {
    
    private final Machine machine;
    private final MachineConfiguration machineConfiguration;
    private final SoftwareConfiguration softwareConfiguration;
    
    public SelectionData(Machine machine, AtomicInteger machineLoadingCount, Software software, DeviceController deviceController) {
        if (machine != null && machineLoadingCount.get() == 0) {
            this.machine = machine;
            machineConfiguration = MachineConfiguration.buildFromParameters(machine);
            
            if (software != null)
                softwareConfiguration = new SoftwareConfiguration(
                    machine, deviceController.getMachineComponent(), software
                );
            else
                softwareConfiguration = null;
        } else {
            this.machine = null;
            machineConfiguration = null;
            softwareConfiguration = null;
        }
    }

    /**
     * @return the machine
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * @return the machineConfiguration
     */
    public MachineConfiguration getMachineConfiguration() {
        return machineConfiguration;
    }

    /**
     * @return the softwareConfiguration
     */
    public SoftwareConfiguration getSoftwareConfiguration() {
        return softwareConfiguration;
    }
    
    public boolean hasSelection() {
        return machine != null;
    }
}
