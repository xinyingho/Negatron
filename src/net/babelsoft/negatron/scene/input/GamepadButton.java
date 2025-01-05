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
 * The list of buttons available on a gamepad
 *
 * For controllers that use a diamond pattern for the face buttons, the
 * south/east/west/north buttons below correspond to the locations in the
 * diamond pattern. For Xbox controllers, this would be A/B/X/Y, for Nintendo
 * Switch controllers, this would be B/A/Y/X, for PlayStation controllers this
 * would be Cross/Circle/Square/Triangle.
 *
 * For controllers that don't use a diamond pattern for the face buttons, the
 * south/east/west/north buttons indicate the buttons labeled A, B, C, D, or
 * 1, 2, 3, 4, or for controllers that aren't labeled, they are the primary,
 * secondary, etc. buttons.
 *
 * The activate action is often the south button and the cancel action is
 * often the east button, but in some regions this is reversed, so your game
 * should allow remapping actions based on user preferences.
 *
 * You can query the labels for the face buttons using
 * SDL_GetGamepadButtonLabel()
 *
 * \since This enum is available since SDL 3.1.3.
 * @author Xiny
 */
public enum GamepadButton {
    INVALID, // = -1,
    SOUTH,           /**< Bottom face button (e.g. Xbox A button) */
    EAST,            /**< Right face button (e.g. Xbox B button) */
    WEST,            /**< Left face button (e.g. Xbox X button) */
    NORTH,           /**< Top face button (e.g. Xbox Y button) */
    BACK,
    GUIDE,
    START,
    LEFT_STICK,
    RIGHT_STICK,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    DPAD_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    MISC1,           /**< Additional button (e.g. Xbox Series X share button, PS5 microphone button, Nintendo Switch Pro capture button, Amazon Luna microphone button, Google Stadia capture button) */
    RIGHT_PADDLE1,   /**< Upper or primary paddle, under your right hand (e.g. Xbox Elite paddle P1) */
    LEFT_PADDLE1,    /**< Upper or primary paddle, under your left hand (e.g. Xbox Elite paddle P3) */
    RIGHT_PADDLE2,   /**< Lower or secondary paddle, under your right hand (e.g. Xbox Elite paddle P2) */
    LEFT_PADDLE2,    /**< Lower or secondary paddle, under your left hand (e.g. Xbox Elite paddle P4) */
    TOUCHPAD,        /**< PS4/PS5 touchpad button */
    MISC2,           /**< Additional button */
    MISC3,           /**< Additional button */
    MISC4,           /**< Additional button */
    MISC5,           /**< Additional button */
    MISC6,           /**< Additional button */
    COUNT,
    LEFT_TRIGGER,    /**< Negatron custom buttons to take into account gamepads that don't have analog but digital triggers */
    RIGHT_TRIGGER;
    
    private static final Map<Byte, GamepadButton> map;
    
    static {
        map = new HashMap<>();
        Arrays.stream(GamepadButton.values()).forEach(
            entry -> map.put((byte)(entry.ordinal() - 1), entry)
        );
    }
    
    public static GamepadButton fromSDL(byte id) {
        return map.get(id);
    }
}
