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
 *
 * @author Xiny
 */
public enum JoystickHatPosition {
    CENTERED    (0),
    UP          (1),
    RIGHT       (2),
    DOWN        (4),
    LEFT        (8),
    RIGHTUP     (3), // RIGHT | UP
    RIGHTDOWN   (6), // RIGHT | DOWN
    LEFTUP      (9), // LEFT | UP
    LEFTDOWN    (12) // LEFT | DOWN
    ;
    
    private static final Map<Byte, JoystickHatPosition> map;
    
    static {
        map = new HashMap<>();
        Arrays.stream(JoystickHatPosition.values()).forEach(
            entry -> map.put(entry.id, entry)
        );
    }
    
    public static JoystickHatPosition fromSDL(byte id) {
        return map.get(id);
    }
    
    private final byte id;
    
    JoystickHatPosition(int SDL_id) {
        id = (byte)SDL_id;
    }
}
