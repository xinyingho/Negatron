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
 * The list of axes available on a gamepad
 *
 * Thumbstick axis values range from SDL_JOYSTICK_AXIS_MIN to
 * SDL_JOYSTICK_AXIS_MAX, and are centered within ~8000 of zero, though
 * advanced UI will allow users to set or autodetect the dead zone, which
 * varies between gamepads.
 *
 * Trigger axis values range from 0 (released) to SDL_JOYSTICK_AXIS_MAX (fully
 * pressed) when reported by SDL_GetGamepadAxis(). Note that this is not the
 * same range that will be reported by the lower-level SDL_GetJoystickAxis().
 *
 * @since This enum is available since SDL 3.1.3.
 * @author Xiny
 */
public enum GamepadAxis {
    INVALID, // = -1,
    LEFTX,
    LEFTY,
    RIGHTX,
    RIGHTY,
    LEFT_TRIGGER,
    RIGHT_TRIGGER,
    COUNT;
    
    private static final Map<Byte, GamepadAxis> map;
    
    static {
        map = new HashMap<>();
        Arrays.stream(GamepadAxis.values()).forEach(
            entry -> map.put((byte)(entry.ordinal() - 1), entry)
        );
    }
    
    /**
    * The largest value an SDL_Joystick's axis can report.
    *
    * @since This macro is available since SDL 3.1.3.
    *
    * @see MIN
    */
    public static final short MAX = 32767;

    /**
     * The smallest value an SDL_Joystick's axis can report.
     *
     * This is a negative number!
     *
     * @since This macro is available since SDL 3.1.3.
     *
     * @see MAX
     */
    public static final short MIN = -32768;
    
    public static GamepadAxis fromSDL(byte id) {
        return map.get(id);
    }
}
