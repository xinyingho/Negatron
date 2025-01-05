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
package net.babelsoft.negatron.scene;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;
import static net.babelsoft.negatron.io.Gamepad.*;
import net.babelsoft.negatron.io.Gamepad.SDL_InitFlags;
import net.babelsoft.negatron.io.event.SDL_Event;
import net.babelsoft.negatron.io.event.SDL_EventType;
import net.babelsoft.negatron.io.event.gamepad.SDL_GamepadAxisEvent;
import net.babelsoft.negatron.io.event.gamepad.SDL_GamepadButtonEvent;
import net.babelsoft.negatron.io.event.gamepad.SDL_GamepadDeviceEvent;
import net.babelsoft.negatron.io.event.gamepad.SDL_GamepadTouchpadEvent;
import net.babelsoft.negatron.io.event.joystick.SDL_JoyAxisEvent;
import net.babelsoft.negatron.io.event.joystick.SDL_JoyButtonEvent;
import net.babelsoft.negatron.io.event.joystick.SDL_JoyDeviceEvent;
import net.babelsoft.negatron.io.event.joystick.SDL_JoyHatEvent;
import net.babelsoft.negatron.scene.event.GamepadEvent;
import net.babelsoft.negatron.scene.event.JoystickEvent;
import net.babelsoft.negatron.scene.input.GamepadAxis;
import net.babelsoft.negatron.scene.input.GamepadButton;
import net.babelsoft.negatron.scene.input.GamepadType;
import net.babelsoft.negatron.scene.input.JoystickHatPosition;
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.util.Shell;

/**
 *
 * @author Xiny
 */
@DefaultProperty("root")
public class Scene extends javafx.scene.Scene implements Disposable {
    
    private MemorySegment event;
    private final List<Integer> gamepadIds;
    private final Robot robot;
    private int axisMotionEventCount;
    
    private record TriggerId(int gamepadId, GamepadAxis trigger) { }
    private final Map<TriggerId, LinkedList<Short>> triggerStates;
    
    private enum InputMode { MOUSE, KEYBOARD }
    private InputMode mode;
    
    // Minimum Apple CoreGraphics implementation to get mouse wheel tilting events on macOS
    private interface CoreGraphics extends Library {
        CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);
        
        class CGEventSourceRef extends PointerType {
            public CGEventSourceRef() { super(); }
            public CGEventSourceRef(Pointer p) { super(p); }
        }
        
        class CGEventRef extends CoreFoundation.CFTypeRef {
            public CGEventRef() { super(); }
            public CGEventRef(Pointer p) { super(p); }
        }

        CoreGraphics.CGEventRef CGEventCreateScrollWheelEvent(
            CoreGraphics.CGEventSourceRef source, int units, int wheelCount, int wheel1, Object... varargs
        );
        void CGEventPost(int CGEventTapLocation, CoreGraphics.CGEventRef event);
    }
    
    public Scene(Parent parent) {
        super(parent);
        
        axisMotionEventCount = 0;
        mode = InputMode.MOUSE;
        
        boolean canPollEvents;
        try {
            canPollEvents = SDL_Init(SDL_InitFlags.SDL_INIT_GAMEPAD);
            // SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_PS5_RUMBLE, ENABLED);
        } catch (Throwable t) {
            Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, "Error while initialising SDL3", t);
            canPollEvents = false;
        }
        
        if (canPollEvents) {
            Arena arena = Arena.ofAuto();
            event = SDL_Event.allocate(arena);
            gamepadIds = new ArrayList<>();
            robot = new Robot();
            triggerStates = new HashMap<>();
            
            // Managing gamepad events in a separate thread will most likely
            // generate multithreading issues with the JavaFX application thread,
            // especially with tooltips and their animation timelines.
            //
            // To mitigate this, simply manage them in the application thread and
            // synchronise events polling with pulse events of the Glass windowing toolkit.
            
            addPostLayoutPulseListener(this::pollEvents);
        } else {
            event = null;
            gamepadIds = null;
            robot = null;
            triggerStates = null;
        }
    }
    
    private boolean isFocused() {
        return !Stage.getWindows().filtered(window -> window.isFocused()).isEmpty();
    }
    
    private void pollEvents() { try {
        while (SDL_PollEvent(event)) {
            int type = SDL_Event.type(event);
            
            switch (type) {
                case SDL_EventType.SDL_EVENT_JOYSTICK_ADDED -> {
                    final int which = SDL_JoyDeviceEvent.which(SDL_Event.jdevice(event));
                    MemorySegment joystick = SDL_OpenJoystick(which);
                    
                    if (joystick.address() == 0L) {
                        processJoystickEvent(JoystickEvent.JOYSTICK_ADDED_ERROR, Integer.toString(which), SDL_GetError());
                        SDL_ClearError();
                    } else
                        processJoystickEvent(JoystickEvent.JOYSTICK_ADDED, SDL_GetJoystickName(joystick), SDL_GetJoystickPath(joystick));
                }
                case SDL_EventType.SDL_EVENT_GAMEPAD_ADDED -> {
                    final int which = SDL_GamepadDeviceEvent.which(SDL_Event.gdevice(event));
                    MemorySegment gamepad = SDL_OpenGamepad(which);
                    
                    if (gamepad.address() == 0L) {
                        processJoystickEvent(JoystickEvent.JOYSTICK_GAMEPAD_ERROR, Integer.toString(which), SDL_GetError());
                        SDL_ClearError();
                    } else {
                        processJoystickEvent(JoystickEvent.JOYSTICK_GAMEPAD, SDL_GetGamepadName(gamepad), SDL_GetGamepadPath(gamepad));
                        gamepadIds.add(which); // gamepad added and opened successfully
                        /*
                        boolean accel = SDL_GamepadHasSensor(gamepad, SDL_SensorType.SDL_SENSOR_ACCEL);
                        boolean gyro = SDL_GamepadHasSensor(gamepad, SDL_SensorType.SDL_SENSOR_GYRO);
                        System.out.println("accel: " + accel + " gyro:" + gyro);
                        if (accel && gyro) {
                            boolean res = SDL_SetGamepadSensorEnabled(gamepad, SDL_SensorType.SDL_SENSOR_ACCEL, accel);
                            if (!res)
                                System.out.println("accel not good: " + SDL_GetError());
                            else
                                System.out.println("accel good");
                        }*/
                    }
                }
                case SDL_EventType.SDL_EVENT_JOYSTICK_REMOVED -> {
                    final int which = SDL_JoyDeviceEvent.which(SDL_Event.jdevice(event));
                    MemorySegment joystick = SDL_GetJoystickFromID(which);
                    
                    if (joystick.address() > 0L) {
                        processJoystickEvent(JoystickEvent.JOYSTICK_REMOVED, SDL_GetJoystickName(joystick), SDL_GetJoystickPath(joystick));
                        SDL_CloseJoystick(joystick);  /* the joystick was unplugged. */
                    }
                }
                case SDL_EventType.SDL_EVENT_GAMEPAD_REMOVED -> {
                    final int which = SDL_GamepadDeviceEvent.which(SDL_Event.gdevice(event));
                    MemorySegment gamepad = SDL_GetGamepadFromID(which);
                    
                    if (gamepad.address() > 0L) {
                        gamepadIds.remove((Integer) which);
                        SDL_CloseGamepad(gamepad);  /* the joystick was unplugged. */
                    }
                }
                case SDL_EventType.SDL_EVENT_JOYSTICK_BATTERY_UPDATED -> {/*
                    final int which = SDL_JoyDeviceEvent.which(SDL_Event.jdevice(event));
                    MemorySegment joystickBatteryEvent = SDL_Event.jbattery(event);
                    int state = SDL_JoyBatteryEvent.state(joystickBatteryEvent);
                    int percent = SDL_JoyBatteryEvent.percent(joystickBatteryEvent);*/
                }
                case SDL_EventType.SDL_EVENT_JOYSTICK_AXIS_MOTION, SDL_EventType.SDL_EVENT_JOYSTICK_HAT_MOTION,
                SDL_EventType.SDL_EVENT_JOYSTICK_BUTTON_DOWN, SDL_EventType.SDL_EVENT_JOYSTICK_BUTTON_UP -> {
                    if (!isFocused())
                        continue; // when Negatron is out of focus, ignore joystick events
                    
                    final int which = SDL_JoyDeviceEvent.which(SDL_Event.jdevice(event));
                    if (gamepadIds.contains(which))
                        continue; // SDL duplicates all the events of a gamepad as joystick and gamepad events, so ignore all the duplicated joystick events

                    switch (type) {
                        case SDL_EventType.SDL_EVENT_JOYSTICK_AXIS_MOTION -> {
                            MemorySegment joystickAxisEvent = SDL_Event.jaxis(event);
                            byte axis = SDL_JoyAxisEvent.axis(joystickAxisEvent);
                            short value = SDL_JoyAxisEvent.value(joystickAxisEvent);

                            GamepadAxis gaxis = switch (axis) {
                                case 0 -> GamepadAxis.LEFTX;
                                case 1 -> GamepadAxis.LEFTY;
                                case 2 -> GamepadAxis.RIGHTX;
                                case 3 -> GamepadAxis.RIGHTY;
                                default -> GamepadAxis.INVALID;
                            };

                            if (gaxis != GamepadAxis.INVALID)
                                handleGamepadAxisMotionEvent(GamepadType.STANDARD, gaxis, value);
                        }
                        case SDL_EventType.SDL_EVENT_JOYSTICK_HAT_MOTION -> {
                            MemorySegment joystickHatEvent = SDL_Event.jhat(event);
                            //byte hatIndex = SDL_JoyHatEvent.hat(joystickHatEvent);
                            JoystickHatPosition hatPosition = JoystickHatPosition.fromSDL( SDL_JoyHatEvent.value(joystickHatEvent) );
                            
                            switch (hatPosition) {
                                case JoystickHatPosition.DOWN -> robot.keyType(KeyCode.DOWN);
                                case JoystickHatPosition.LEFT -> robot.keyType(KeyCode.LEFT);
                                case JoystickHatPosition.RIGHT -> robot.keyType(KeyCode.RIGHT);
                                case JoystickHatPosition.UP -> robot.keyType(KeyCode.UP);
                            }
                            mode = InputMode.KEYBOARD;
                        }
                        case SDL_EventType.SDL_EVENT_JOYSTICK_BUTTON_DOWN -> handleJoystickButtonEvent(
                            type, event, false,
                            key -> robot.keyPress(key), button -> robot.mousePress(button)
                        );
                        case SDL_EventType.SDL_EVENT_JOYSTICK_BUTTON_UP -> handleJoystickButtonEvent(
                            type, event, true,
                            key -> robot.keyRelease(key), button -> robot.mouseRelease(button)
                        );
                    }
                }
                case SDL_EventType.SDL_EVENT_GAMEPAD_AXIS_MOTION -> {
                    if (!isFocused())
                        continue; // when Negatron is out of focus, ignore gamepad events
                    
                    MemorySegment gamepadAxisEvent = SDL_Event.gaxis(event);
                    GamepadAxis axis = GamepadAxis.fromSDL( SDL_GamepadAxisEvent.axis(gamepadAxisEvent) );
                    short value = SDL_GamepadAxisEvent.value(gamepadAxisEvent);

                    switch (axis) {
                        default -> {
                            GamepadType gamepadType = GamepadType.fromSDL(
                                SDL_GetGamepadTypeForID(SDL_GamepadAxisEvent.which(gamepadAxisEvent))
                            );
                            handleGamepadAxisMotionEvent(gamepadType, axis, value);
                        }
                        case GamepadAxis.LEFT_TRIGGER, GamepadAxis.RIGHT_TRIGGER -> {
                            int which = SDL_GamepadAxisEvent.which(gamepadAxisEvent);
                            TriggerId id = new TriggerId(which, axis);
                            
                            if (!triggerStates.containsKey(id))
                                triggerStates.put(id, new LinkedList<>());
                            LinkedList<Short> queue = triggerStates.get(id);
                            
                            // Nintendo Switch Pro controllers' triggers are actually digital buttons.
                            // They are still recognised as analogue triggers, only sending the values MAX then 0 when pushed.
                            boolean processEvent = (value == GamepadAxis.MAX);
                            
                            queue.offer(value);
                            if (queue.size() == 7) { // at least 7 values to get a good crest and ignore malformed waves i.e. hesitant moves
                                if (
                                    queue.get(3) > 5000 && // if the peak is anything below, this is surely just a wrong flick
                                    queue.get(0) < queue.get(1) && queue.get(1) < queue.get(2) && queue.get(2) < queue.get(3) &&
                                    queue.get(3) > queue.get(4) && queue.get(4) > queue.get(5) && queue.get(5) > queue.get(6)
                                ) {
                                    processEvent = true;
                                    System.out.println("process evt");
                                }
                                queue.poll();
                            }
                            
                            if (processEvent) processGamepadEvent(
                                axis == GamepadAxis.LEFT_TRIGGER ? GamepadButton.LEFT_TRIGGER : GamepadButton.RIGHT_TRIGGER
                            );
                        }
                    }
                }
                case SDL_EventType.SDL_EVENT_GAMEPAD_BUTTON_DOWN -> handleGamepadButtonEvent(
                    type, event, false,
                    key -> robot.keyPress(key), button -> robot.mousePress(button)
                );
                case SDL_EventType.SDL_EVENT_GAMEPAD_BUTTON_UP -> handleGamepadButtonEvent(
                    type, event, true,
                    key -> robot.keyRelease(key), button -> robot.mouseRelease(button)
                );
                case SDL_EventType.SDL_EVENT_GAMEPAD_TOUCHPAD_DOWN, SDL_EventType.SDL_EVENT_GAMEPAD_TOUCHPAD_UP,
                SDL_EventType.SDL_EVENT_GAMEPAD_TOUCHPAD_MOTION -> {
                    if (!isFocused())
                        continue; // when Negatron is out of focus, ignore gamepad events
                    
                    MemorySegment gamepadTouchpadEvent = SDL_Event.gtouchpad(event);
                    int touchpad = SDL_GamepadTouchpadEvent.touchpad(gamepadTouchpadEvent);
                    int finger = SDL_GamepadTouchpadEvent.finger(gamepadTouchpadEvent);
                    float x = SDL_GamepadTouchpadEvent.x(gamepadTouchpadEvent);
                    float y = SDL_GamepadTouchpadEvent.y(gamepadTouchpadEvent);
                    float pressure = SDL_GamepadTouchpadEvent.pressure(gamepadTouchpadEvent);
                    System.out.println("SDL event: " + type + " touchIndex:" + touchpad + " finger:" + finger + " x:" + x +" y:" + y + " pressure:" + pressure);
                    //robot.mouseMove(x, y);
                }
                // case SDL_EventType.SDL_EVENT_GAMEPAD_SENSOR_UPDATE
                // case SDL_EventType.SDL_EVENT_JOYSTICK_BALL_MOTION
                // case SDL_EventType.SDL_EVENT_JOYSTICK_UPDATE_COMPLETE
                // case SDL_EventType.SDL_EVENT_GAMEPAD_UPDATE_COMPLETE
            }
        }
    } catch (Throwable t) {
        Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, "Error while polling gamepad events", t);
        t.printStackTrace();
    } }
    
    private void handleGamepadAxisMotionEvent(GamepadType gamepadType, GamepadAxis axis, short value) throws Throwable {
        final short DEADZONE = gamepadType.getDeadzone();
        if (-DEADZONE < value && value < DEADZONE)
            return; // ignore values in the gamepad's deadzone

        final int MAX = GamepadAxis.MAX - DEADZONE;
        final int THRESHOLD_1 = MAX / 4;
        final int THRESHOLD_2 = MAX / 2;
        final int THRESHOLD_3 = MAX - MAX / 4;

        int move = value > 0 ? value - DEADZONE : value + DEADZONE;
        
        switch (axis) {
            case GamepadAxis.LEFTX, GamepadAxis.LEFTY -> {
                Point2D mousePosition = robot.getMousePosition();
                // managing multiscreen PC setups: detect the monitor in which the mouse is
                ObservableList<Screen> screens = Screen.getScreens();
                Screen screen = screens.size() > 1 ?
                    screens.filtered(s -> s.getBounds().contains(mousePosition)).getFirst()
                    :
                    screens.getFirst()
                ;

                double differential = move * screen.getBounds().getWidth() / MAX;
                move = Math.abs(move);

                if (move < THRESHOLD_1)
                    differential /= 1000d;
                else if (move < THRESHOLD_2)
                    differential /= 500d;
                else if (move < THRESHOLD_3)
                    differential /= 100d;
                else
                    differential /= 50d;

                if (axis == GamepadAxis.LEFTX)
                    robot.mouseMove( mousePosition.add(differential, 0) );
                else // GamepadAxis.LEFTY
                    robot.mouseMove( mousePosition.add(0, differential) );
                mode = InputMode.MOUSE;
            }
            case GamepadAxis.RIGHTX -> {
                int amplitude; {
                    int absMove = Math.abs(move);
                    if (absMove < THRESHOLD_1)
                        return; // define a larger deadzone within which users probably just want to scroll up or down instead of scrolling left or right.
                    else if (absMove < THRESHOLD_2)
                        amplitude = 1;
                    else if (absMove < THRESHOLD_3)
                        amplitude = 4;
                    else
                        amplitude = 8;
                }

                // JavaFX robot does not have a method to synthesise mouse wheel tilting events
                // i.e. horizontal wheel movements as opposed to vertical wheel scrolling).
                // So, directly use native OS functions using JNA (a dependency that comes with VLCj).
                if (Shell.isWindows()) {
                    WinUser.INPUT input = new WinUser.INPUT();
                    input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
                    input.input.setType(WinUser.MOUSEINPUT.class);
                    input.input.mi.time = new WinDef.DWORD(0);
                    input.input.mi.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

                    final int MOUSEEVENTF_HWHEEL = 0x01000;
                    int direction = amplitude * Integer.signum(move);
                    input.input.mi.dwFlags = new WinDef.DWORD(MOUSEEVENTF_HWHEEL);
                    input.input.mi.mouseData = new WinDef.DWORD(direction);

                    User32.INSTANCE.SendInput(
                        new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size()
                    );
                } else if (Shell.isMacOs()) {
                    int direction = amplitude * Integer.signum(move) * -1;

                    final int kCGScrollEventUnitPixel = 0;
                    final int kCGHIDEventTap = 0;
                    CoreGraphics.CGEventRef cgEvent = CoreGraphics.INSTANCE.CGEventCreateScrollWheelEvent(
                        null, kCGScrollEventUnitPixel, 2, 0, direction
                    );
                    CoreGraphics.INSTANCE.CGEventPost(kCGHIDEventTap, cgEvent);
                    cgEvent.release();
                } else if (Shell.isLinux()) {
                    final int MOUSEWHEEL_TILT_LEFT = 6;
                    final int MOUSEWHEEL_TILT_RIGHT = 7;
                    int direction = move > 0 ? MOUSEWHEEL_TILT_RIGHT : MOUSEWHEEL_TILT_LEFT;

                    X11.Display display = X11.INSTANCE.XOpenDisplay(null);
                    for (int i = 0; i < amplitude; ++i) {
                        X11.XTest.INSTANCE.XTestFakeButtonEvent(display, direction, true, new NativeLong(0));
                        X11.XTest.INSTANCE.XTestFakeButtonEvent(display, direction, false, new NativeLong(0));
                    }
                    X11.INSTANCE.XSync(display, false);
                    X11.INSTANCE.XCloseDisplay(display);
                }
            }
            case GamepadAxis.RIGHTY -> {
                int amplitude = Math.abs(move);

                if (amplitude < THRESHOLD_1)
                    return; // define a larger deadzone within which users probably just want to scroll left or right instead of scrolling up or down
                else if (amplitude < THRESHOLD_2) {
                    if (axisMotionEventCount++ < 10)
                        return; // as gamepad axis motions send many events even for a small pinch, slown down the pace with this counting trick
                    axisMotionEventCount = 0;
                    amplitude = 1;
                } else if (amplitude < THRESHOLD_3)
                    amplitude = 1;
                else
                    amplitude = 10;
                
                if (Shell.isMacOs())
                    amplitude *= -1;

                robot.mouseWheel(amplitude * Integer.signum(move));
            }
        }
    }
    
    private void handleJoystickButtonEvent(
        int type, MemorySegment event, boolean clicked,
        Consumer<KeyCode> keyboard, Consumer<MouseButton> mouse
    ) {
        MemorySegment joystickButtonEvent = SDL_Event.jbutton(event);
        byte button = SDL_JoyButtonEvent.button(joystickButtonEvent);
        
        final Consumer<GamepadButton> triggerButtonClicked = clickedbutton -> {
            if (clicked)
                processGamepadEvent(clickedbutton);
        };
        
        // Button mapping suitable for PDP REALMz controllers for Nintendo Switch, the only tested gamepad model that is not recognised as a gamepad in Windows 10
        switch (button) {
            case 0 -> triggerButtonClicked.accept(GamepadButton.WEST); // display the favourites pane
            case 1 -> mouse.accept(MouseButton.PRIMARY); // SOUTH
            case 2 -> { if (mode == InputMode.MOUSE) mouse.accept(MouseButton.SECONDARY); else keyboard.accept(KeyCode.SPACE); } // EAST
            case 3 -> { if (mode == InputMode.MOUSE) mouse.accept(MouseButton.MIDDLE); else keyboard.accept(KeyCode.F2); } // NORTH
            case 4 -> triggerButtonClicked.accept(GamepadButton.LEFT_SHOULDER); // cycle between the different display types of the current pane (maximised, normal, minimised)
            case 5 -> keyboard.accept(KeyCode.ESCAPE); // RIGHT_SHOULDER
            case 6 -> triggerButtonClicked.accept(GamepadButton.LEFT_TRIGGER);
            case 7 -> triggerButtonClicked.accept(GamepadButton.RIGHT_TRIGGER);
            case 8 -> triggerButtonClicked.accept(GamepadButton.BACK); // display the general configuration pane
            case 9 -> triggerButtonClicked.accept(GamepadButton.START); // start MAME
            case 10 -> triggerButtonClicked.accept(GamepadButton.LEFT_STICK); // display logs
            case 11 -> triggerButtonClicked.accept(GamepadButton.RIGHT_STICK); // display the advanced parametrisation pane / scroll back to the focused cell of the list
            case 12 -> triggerButtonClicked.accept(GamepadButton.GUIDE); // display Negatron's help
        }
    }
    
    private void handleGamepadButtonEvent(
        int type, MemorySegment event, boolean clicked,
        Consumer<KeyCode> keyboard, Consumer<MouseButton> mouse
    ) {
        if (!isFocused())
            return; // when Negatron is out of focus, ignore gamepad events

        MemorySegment gamepadButtonEvent = SDL_Event.gbutton(event);
        GamepadButton button = GamepadButton.fromSDL( SDL_GamepadButtonEvent.button(gamepadButtonEvent) );
        
        switch (button) {
            case GamepadButton.SOUTH -> mouse.accept(MouseButton.PRIMARY);
            case GamepadButton.EAST -> { if (mode == InputMode.MOUSE) mouse.accept(MouseButton.SECONDARY); else keyboard.accept(KeyCode.SPACE); }
            case GamepadButton.NORTH -> { if (mode == InputMode.MOUSE) mouse.accept(MouseButton.MIDDLE); else keyboard.accept(KeyCode.F2); }
            // case GamepadButton.WEST -> {} // display the favourites pane
            case GamepadButton.DPAD_DOWN -> { keyboard.accept(KeyCode.DOWN); mode = InputMode.KEYBOARD; }
            case GamepadButton.DPAD_LEFT -> { keyboard.accept(KeyCode.LEFT); mode = InputMode.KEYBOARD; }
            case GamepadButton.DPAD_RIGHT -> { keyboard.accept(KeyCode.RIGHT); mode = InputMode.KEYBOARD; }
            case GamepadButton.DPAD_UP -> { keyboard.accept(KeyCode.UP); mode = InputMode.KEYBOARD; }
            // case GamepadButton.LEFT_SHOULDER -> {} // cycle between the different display types of the current pane (maximised, normal, minimised)
            case GamepadButton.RIGHT_SHOULDER -> keyboard.accept(KeyCode.ESCAPE);
            // case GamepadButton.START -> {} // start MAME
            // case GamepadButton.BACK -> {} // display the general configuration pane
            // case GamepadButton.GUIDE -> {} // display Negatron's help
            // case GamepadButton.LEFT_STICK -> {} // display logs
            // case GamepadButton.RIGHT_STICK -> {} // display the advanced parametrisation pane / scroll back to the focused cell of the list
            default -> {
                if (clicked)
                    processGamepadEvent(button);
            }
        }
    }
    
    private EventTarget findEventTarget() {
        final Node sceneFocusOwner = getFocusOwner();

        // send events to the current focus owner or to scene if
        // the focus owner is not set
        return
            sceneFocusOwner != null && sceneFocusOwner.getScene() == Scene.this ?
            sceneFocusOwner : Scene.this
        ;
    }
    
    private void processJoystickEvent(EventType<JoystickEvent> eventType, String name, String info) {
        final EventTarget eventTarget = findEventTarget();
        Event.fireEvent(eventTarget, new JoystickEvent(null, eventTarget, eventType, name, info));
    }
    
    private void processGamepadEvent(GamepadButton button) {
        final EventTarget eventTarget = findEventTarget();
        Event.fireEvent(eventTarget, new GamepadEvent(null, eventTarget, button));
    }

    @Override
    public void dispose() {
        try {
            SDL_Quit();
        } catch(Throwable t) {
            // swallow any exceptions
        }
    }
}
