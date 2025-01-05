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
package net.babelsoft.negatron.io.event;

/**
 * {@snippet lang=c :
 * enum SDL_EventType
 * }
 * @author Xiny
 */
public class SDL_EventType {
    
    public static final int SDL_EVENT_FIRST = 0;
    public static final int SDL_EVENT_QUIT = 256;
    public static final int SDL_EVENT_TERMINATING = 257;
    public static final int SDL_EVENT_LOW_MEMORY = 258;
    public static final int SDL_EVENT_WILL_ENTER_BACKGROUND = 259;
    public static final int SDL_EVENT_DID_ENTER_BACKGROUND = 260;
    public static final int SDL_EVENT_WILL_ENTER_FOREGROUND = 261;
    public static final int SDL_EVENT_DID_ENTER_FOREGROUND = 262;
    public static final int SDL_EVENT_LOCALE_CHANGED = 263;
    public static final int SDL_EVENT_SYSTEM_THEME_CHANGED = 264;
    public static final int SDL_EVENT_DISPLAY_ORIENTATION = 337;
    public static final int SDL_EVENT_DISPLAY_ADDED = 338;
    public static final int SDL_EVENT_DISPLAY_REMOVED = 339;
    public static final int SDL_EVENT_DISPLAY_MOVED = 340;
    public static final int SDL_EVENT_DISPLAY_DESKTOP_MODE_CHANGED = 341;
    public static final int SDL_EVENT_DISPLAY_CURRENT_MODE_CHANGED = 342;
    public static final int SDL_EVENT_DISPLAY_CONTENT_SCALE_CHANGED = 343;
    public static final int SDL_EVENT_DISPLAY_FIRST = 337;
    public static final int SDL_EVENT_DISPLAY_LAST = 343;
    public static final int SDL_EVENT_WINDOW_SHOWN = 514;
    public static final int SDL_EVENT_WINDOW_HIDDEN = 515;
    public static final int SDL_EVENT_WINDOW_EXPOSED = 516;
    public static final int SDL_EVENT_WINDOW_MOVED = 517;
    public static final int SDL_EVENT_WINDOW_RESIZED = 518;
    public static final int SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED = 519;
    public static final int SDL_EVENT_WINDOW_METAL_VIEW_RESIZED = 520;
    public static final int SDL_EVENT_WINDOW_MINIMIZED = 521;
    public static final int SDL_EVENT_WINDOW_MAXIMIZED = 522;
    public static final int SDL_EVENT_WINDOW_RESTORED = 523;
    public static final int SDL_EVENT_WINDOW_MOUSE_ENTER = 524;
    public static final int SDL_EVENT_WINDOW_MOUSE_LEAVE = 525;
    public static final int SDL_EVENT_WINDOW_FOCUS_GAINED = 526;
    public static final int SDL_EVENT_WINDOW_FOCUS_LOST = 527;
    public static final int SDL_EVENT_WINDOW_CLOSE_REQUESTED = 528;
    public static final int SDL_EVENT_WINDOW_HIT_TEST = 529;
    public static final int SDL_EVENT_WINDOW_ICCPROF_CHANGED = 530;
    public static final int SDL_EVENT_WINDOW_DISPLAY_CHANGED = 531;
    public static final int SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED = 532;
    public static final int SDL_EVENT_WINDOW_SAFE_AREA_CHANGED = 533;
    public static final int SDL_EVENT_WINDOW_OCCLUDED = 534;
    public static final int SDL_EVENT_WINDOW_ENTER_FULLSCREEN = 535;
    public static final int SDL_EVENT_WINDOW_LEAVE_FULLSCREEN = 536;
    public static final int SDL_EVENT_WINDOW_DESTROYED = 537;
    public static final int SDL_EVENT_WINDOW_HDR_STATE_CHANGED = 538;
    public static final int SDL_EVENT_WINDOW_FIRST = 514;
    public static final int SDL_EVENT_WINDOW_LAST = 538;
    public static final int SDL_EVENT_KEY_DOWN = 768;
    public static final int SDL_EVENT_KEY_UP = 769;
    public static final int SDL_EVENT_TEXT_EDITING = 770;
    public static final int SDL_EVENT_TEXT_INPUT = 771;
    public static final int SDL_EVENT_KEYMAP_CHANGED = 772;
    public static final int SDL_EVENT_KEYBOARD_ADDED = 773;
    public static final int SDL_EVENT_KEYBOARD_REMOVED = 774;
    public static final int SDL_EVENT_TEXT_EDITING_CANDIDATES = 775;
    public static final int SDL_EVENT_MOUSE_MOTION = 1024;
    public static final int SDL_EVENT_MOUSE_BUTTON_DOWN = 1025;
    public static final int SDL_EVENT_MOUSE_BUTTON_UP = 1026;
    public static final int SDL_EVENT_MOUSE_WHEEL = 1027;
    public static final int SDL_EVENT_MOUSE_ADDED = 1028;
    public static final int SDL_EVENT_MOUSE_REMOVED = 1029;
    public static final int SDL_EVENT_JOYSTICK_AXIS_MOTION = 1536;
    public static final int SDL_EVENT_JOYSTICK_BALL_MOTION = 1537;
    public static final int SDL_EVENT_JOYSTICK_HAT_MOTION = 1538;
    public static final int SDL_EVENT_JOYSTICK_BUTTON_DOWN = 1539;
    public static final int SDL_EVENT_JOYSTICK_BUTTON_UP = 1540;
    public static final int SDL_EVENT_JOYSTICK_ADDED = 1541;
    public static final int SDL_EVENT_JOYSTICK_REMOVED = 1542;
    public static final int SDL_EVENT_JOYSTICK_BATTERY_UPDATED = 1543; /**< Joystick battery level change */
    public static final int SDL_EVENT_JOYSTICK_UPDATE_COMPLETE = 1544;
    public static final int SDL_EVENT_GAMEPAD_AXIS_MOTION = 1616;
    public static final int SDL_EVENT_GAMEPAD_BUTTON_DOWN = 1617;
    public static final int SDL_EVENT_GAMEPAD_BUTTON_UP = 1618;
    public static final int SDL_EVENT_GAMEPAD_ADDED = 1619; /**< A new gamepad has been inserted into the system */
    public static final int SDL_EVENT_GAMEPAD_REMOVED = 1620; /**< A gamepad has been removed */
    public static final int SDL_EVENT_GAMEPAD_REMAPPED = 1621; /**< The gamepad's button layout mapping was updated */
    public static final int SDL_EVENT_GAMEPAD_TOUCHPAD_DOWN = 1622; /**< Gamepad touchpad was touched */
    public static final int SDL_EVENT_GAMEPAD_TOUCHPAD_MOTION = 1623; /**< Gamepad touchpad finger was moved */
    public static final int SDL_EVENT_GAMEPAD_TOUCHPAD_UP = 1624; /**< Gamepad touchpad finger was lifted */
    public static final int SDL_EVENT_GAMEPAD_SENSOR_UPDATE = 1625;
    public static final int SDL_EVENT_GAMEPAD_UPDATE_COMPLETE = 1626; /**< Gamepad events are received in batches. This event indicates the end of a batch. Not very useful when polling events in a loop. */
    public static final int SDL_EVENT_GAMEPAD_STEAM_HANDLE_UPDATED = 1627; /**< Gamepad Steam handle has changed. Triggered when a controller's API handle changes, e.g. the controllers were reassigned slots in the Steam UI */
    public static final int SDL_EVENT_FINGER_DOWN = 1792;
    public static final int SDL_EVENT_FINGER_UP = 1793;
    public static final int SDL_EVENT_FINGER_MOTION = 1794;
    public static final int SDL_EVENT_CLIPBOARD_UPDATE = 2304;
    public static final int SDL_EVENT_DROP_FILE = 4096;
    public static final int SDL_EVENT_DROP_TEXT = 4097;
    public static final int SDL_EVENT_DROP_BEGIN = 4098;
    public static final int SDL_EVENT_DROP_COMPLETE = 4099;
    public static final int SDL_EVENT_DROP_POSITION = 4100;
    public static final int SDL_EVENT_AUDIO_DEVICE_ADDED = 4352;
    public static final int SDL_EVENT_AUDIO_DEVICE_REMOVED = 4353;
    public static final int SDL_EVENT_AUDIO_DEVICE_FORMAT_CHANGED = 4354;
    public static final int SDL_EVENT_SENSOR_UPDATE = 4608;
    public static final int SDL_EVENT_PEN_PROXIMITY_IN = 4864;
    public static final int SDL_EVENT_PEN_PROXIMITY_OUT = 4865;
    public static final int SDL_EVENT_PEN_DOWN = 4866;
    public static final int SDL_EVENT_PEN_UP = 4867;
    public static final int SDL_EVENT_PEN_BUTTON_DOWN = 4868;
    public static final int SDL_EVENT_PEN_BUTTON_UP = 4869;
    public static final int SDL_EVENT_PEN_MOTION = 4870;
    public static final int SDL_EVENT_PEN_AXIS = 4871;
    public static final int SDL_EVENT_CAMERA_DEVICE_ADDED = 5120;
    public static final int SDL_EVENT_CAMERA_DEVICE_REMOVED = 5121;
    public static final int SDL_EVENT_CAMERA_DEVICE_APPROVED = 5122;
    public static final int SDL_EVENT_CAMERA_DEVICE_DENIED = 5123;
    public static final int SDL_EVENT_RENDER_TARGETS_RESET = 8192;
    public static final int SDL_EVENT_RENDER_DEVICE_RESET = 8193;
    public static final int SDL_EVENT_RENDER_DEVICE_LOST = 8194;
    public static final int SDL_EVENT_PRIVATE0 = 16384;
    public static final int SDL_EVENT_PRIVATE1 = 16385;
    public static final int SDL_EVENT_PRIVATE2 = 16386;
    public static final int SDL_EVENT_PRIVATE3 = 16387;
    public static final int SDL_EVENT_POLL_SENTINEL = 32512;
    public static final int SDL_EVENT_USER = 32768;
    public static final int SDL_EVENT_LAST = 65535;
    public static final int SDL_EVENT_ENUM_PADDING = 2147483647;
}
