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
import net.babelsoft.negatron.model.component.MachineElementList;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class MachineConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String commandLine;
    private boolean configurable;
    private MachineElementList parameters;
    
    public MachineConfiguration(final String commandLine) {
        this.commandLine = Strings.orElseBlank(commandLine);
        this.configurable = true;
        parameters = null;
    }
    
    public MachineConfiguration(final MachineElementList parameters, boolean configurable) {
        commandLine = null;
        this.configurable = configurable;
        this.parameters = parameters;
    }
    
    public static MachineConfiguration buildFromCommandLine(final Machine machine) {
        return new MachineConfiguration(machine.toCommandLine());
    }
    
    public static MachineConfiguration buildFromParameters(final Machine machine) {
        return new MachineConfiguration(machine.copyParameters(), machine.isConfigurable());
    }
    
    public boolean isCommandLine() {
        return commandLine != null;
    }

    public String getCommandLine() {
        return commandLine;
    }
    
    public boolean isConfigurable() {
        return configurable;
    }

    public MachineElementList getParameters() {
        return parameters;
    }
}
