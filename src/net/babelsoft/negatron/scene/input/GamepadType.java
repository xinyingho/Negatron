/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2024 BabelSoft S.A.S.U.
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
package net.babelsoft.negatron.scene.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard gamepad types.
 *
 * This type does not necessarily map to first-party controllers from
 * Microsoft/Sony/Nintendo; in many cases, third-party controllers can report
 * as these, either because they were designed for a specific console, or they
 * simply most closely match that console's controllers (does it have A/B/X/Y
 * buttons or X/O/Square/Triangle? Does it have a touchpad? etc).
 *
 * @author Xiny
 */
public enum GamepadType {
    UNKNOWN(8000), /**< +/- 8000 is an overly safe deadzone range */
    STANDARD(3000), /**< +/- 3000 is safe enough for most gamepads */
    XBOX360(6000), /**< An old Xbox 360 coontroller can have a deadzone in the range of -/+ 6000 */
    XBOXONE(1000), /**< Xbox One and Xbox Series controllers usually don't have any deadzones but sometimes a deadzone of -/+ 1000 can appear momentarily */
    PS3(2000), /**< Sixaxis and Dual Shock 3 controllers won't work out of the box on Windows 10 as the default Microsoft driver is crappy. After installing DsHidMini Driver v2.2.282, SDL recognise them as 'STANDARD' controllers with a deadzone of -/+ 2000. */
    PS4(2000), /**< PS4 controllers have a deadzone of -/+ 2000 */
    PS5(2000), /**< PS5 controllers usually don't have any deadzones but sometimes a deadzone of -/+ 2000 can appear momentarily */
    NINTENDO_SWITCH_PRO(6000), /**< A deadzone in the range of -/+ 6000 is suitable for Switch Pro Controllers */
    NINTENDO_SWITCH_JOYCON_LEFT,
    NINTENDO_SWITCH_JOYCON_RIGHT,
    NINTENDO_SWITCH_JOYCON_PAIR,
    COUNT;
    
    private static final Map<Integer, GamepadType> map;
    
    static {
        map = new HashMap<>();
        Arrays.stream(GamepadType.values()).forEach(
            entry -> map.put(entry.ordinal(), entry)
        );
    }
    
    public static GamepadType fromSDL(int id) {
        return map.get(id);
    }
    
    final short deadzone;
    
    GamepadType() {
        this(0);
    }
    
    GamepadType(int deadzone) {
        this.deadzone = (short)deadzone;
    }
    
    public short getDeadzone() {
        return deadzone;
    }
}
