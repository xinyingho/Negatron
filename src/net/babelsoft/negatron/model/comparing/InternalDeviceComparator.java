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

import java.util.Map;

/**
 *
 * @author capan
 */
public class InternalDeviceComparator {
    
    private Map<String, String> oldInternalDevices;
    private boolean hasChanged;
    
    public void compare(Map<String, String> internalDevices) {
        if (oldInternalDevices != null) {
            hasChanged = internalDevices.keySet().stream().anyMatch(
                key -> !oldInternalDevices.containsKey(key)
            );
            
            if (!hasChanged)
                hasChanged = oldInternalDevices.keySet().stream().anyMatch(
                    key -> !internalDevices.containsKey(key)
                );
        } else
            hasChanged = true;
        
        oldInternalDevices = internalDevices;
    }

    public boolean hasChanged() {
        return hasChanged;
    }
}
