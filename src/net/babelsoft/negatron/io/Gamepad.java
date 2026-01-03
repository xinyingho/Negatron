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
package net.babelsoft.negatron.io;

import java.io.File;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.babelsoft.negatron.util.Shell;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author Xiny
 */
public class Gamepad {
    
    public static final ValueLayout.OfBoolean C_BOOL = ValueLayout.JAVA_BOOLEAN;
    public static final ValueLayout.OfByte C_CHAR = ValueLayout.JAVA_BYTE;
    public static final ValueLayout.OfShort C_SHORT = ValueLayout.JAVA_SHORT;
    public static final ValueLayout.OfInt C_INT = ValueLayout.JAVA_INT;
    public static final ValueLayout.OfLong C_LONG_LONG = ValueLayout.JAVA_LONG;
    public static final ValueLayout.OfFloat C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static final ValueLayout.OfDouble C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static final AddressLayout C_POINTER = ValueLayout.ADDRESS.withTargetLayout(
        MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE)
    );
    public static final ValueLayout.OfInt C_LONG = ValueLayout.JAVA_INT;
    public static final ValueLayout.OfDouble C_LONG_DOUBLE = ValueLayout.JAVA_DOUBLE;
    
    static final Arena LIBRARY_ARENA = Arena.ofAuto();
    
    static {
        boolean isLoaded = false;
        String exePath = System.getProperty("jpackage.app-path");
        if (Shell.isLinux() && Strings.isValid(exePath)) try {
            // required for the Linux packaged versions
            Path appPath = Paths.get(exePath).getParent().resolveSibling("lib/app");
            File libSDL = new File(appPath.toString()+ "/" + System.mapLibraryName("SDL3"));
            System.load(libSDL.getAbsolutePath());
            isLoaded = true;
        } catch (UnsatisfiedLinkError e) { }
        
        if (!isLoaded) try {
            File libSDL = new File("./" + System.mapLibraryName("SDL3"));
            System.load(libSDL.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            System.loadLibrary("SDL3");
        }
    }
    
    static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup().or(
        Linker.nativeLinker().defaultLookup()
    );
    
    static MemorySegment findOrThrow(String symbol) {
        return SYMBOL_LOOKUP.find(symbol).orElseThrow(
            () -> new UnsatisfiedLinkError("unresolved symbol: " + symbol)
        );
    }
    
    public class SDL_InitFlags {
        public static final int SDL_INIT_AUDIO    = 0x00010; /**< `SDL_INIT_AUDIO` implies `SDL_INIT_EVENTS` */
        public static final int SDL_INIT_VIDEO    = 0x00020; /**< `SDL_INIT_VIDEO` implies `SDL_INIT_EVENTS` */
        public static final int SDL_INIT_JOYSTICK = 0x00200; /**< `SDL_INIT_JOYSTICK` implies `SDL_INIT_EVENTS`, should be initialized on the same thread as SDL_INIT_VIDEO on Windows if you don't set SDL_HINT_JOYSTICK_THREAD */
        public static final int SDL_INIT_HAPTIC   = 0x01000;
        public static final int SDL_INIT_GAMEPAD  = 0x02000; /**< `SDL_INIT_GAMEPAD` implies `SDL_INIT_JOYSTICK` */
        public static final int SDL_INIT_EVENTS   = 0x04000;
        public static final int SDL_INIT_SENSOR   = 0x08000; /**< `SDL_INIT_SENSOR` implies `SDL_INIT_EVENTS` */
        public static final int SDL_INIT_CAMERA   = 0x10000; /**< `SDL_INIT_CAMERA` implies `SDL_INIT_EVENTS` */
    }

    /**
     * A variable controlling whether extended input reports should be used for
     * PS4 controllers when using the HIDAPI driver.
     *
     * The variable can be set to the following values:
     *
     * - "0": extended reports are not enabled. (default)
     * - "1": extended reports are enabled.
     *
     * Extended input reports allow rumble on Bluetooth PS4 controllers, but break
     * DirectInput handling for applications that don't use SDL.
     *
     * Once extended reports are enabled, they can not be disabled without power
     * cycling the controller.
     *
     * For compatibility with applications written for versions of SDL prior to
     * the introduction of PS5 controller support, this value will also control
     * the state of extended reports on PS5 controllers when the
     * SDL_HINT_JOYSTICK_HIDAPI_PS5_RUMBLE hint is not explicitly set.
     *
     * This hint can be enabled anytime.
     *
     * {@snippet lang=c :
     * #define SDL_HINT_JOYSTICK_HIDAPI_PS4_RUMBLE "SDL_JOYSTICK_HIDAPI_PS4_RUMBLE"
     * }
     * 
     * @since This hint is available since SDL 3.1.3.
     */
    public static final MemorySegment SDL_HINT_JOYSTICK_HIDAPI_PS4_RUMBLE =
        Gamepad.LIBRARY_ARENA.allocateFrom("SDL_JOYSTICK_HIDAPI_PS4_RUMBLE")
    ;
    
    /**
     * A variable controlling whether extended input reports should be used for
     * PS5 controllers when using the HIDAPI driver.
     *
     * The variable can be set to the following values:
     *
     * - "0": extended reports are not enabled. (default)
     * - "1": extended reports.
     *
     * Extended input reports allow rumble on Bluetooth PS5 controllers, but break
     * DirectInput handling for applications that don't use SDL.
     *
     * Once extended reports are enabled, they can not be disabled without power
     * cycling the controller.
     *
     * For compatibility with applications written for versions of SDL prior to
     * the introduction of PS5 controller support, this value defaults to the
     * value of SDL_HINT_JOYSTICK_HIDAPI_PS4_RUMBLE.
     *
     * This hint can be enabled anytime.
     *
     * {@snippet lang=c :
     * #define SDL_HINT_JOYSTICK_HIDAPI_PS5_RUMBLE "SDL_JOYSTICK_HIDAPI_PS5_RUMBLE"
     * }
     * 
     * @since This hint is available since SDL 3.1.3.
     */
    public static final MemorySegment SDL_HINT_JOYSTICK_HIDAPI_PS5_RUMBLE =
        Gamepad.LIBRARY_ARENA.allocateFrom("SDL_JOYSTICK_HIDAPI_PS5_RUMBLE")
    ;
    
    public static final MemorySegment ENABLED = Gamepad.LIBRARY_ARENA.allocateFrom("1");
    
    private static class SDL_SetHint {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_SetHint");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Set a hint with normal priority.
     *
     * Hints will not be set if there is an existing override hint or environment
     * variable that takes precedence. You can use SDL_SetHintWithPriority() to
     * set the hint with override priority instead.
     * 
     * {@snippet lang=c :
     * extern bool SDL_SetHint(const char *name, const char *value)
     * }
     * 
     * \threadsafety It is safe to call this function from any thread.
     *
     * @param name the hint to set.
     * @param value the value of the hint variable.
     * @return true on success or false on failure; call SDL_GetError() for more
     *          information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetHint
     * @see #SDL_ResetHint
     * @see #SDL_SetHintWithPriority
     */
    public static boolean SDL_SetHint(MemorySegment name, MemorySegment value) throws Throwable {
        return (boolean)SDL_SetHint.HANDLE.invokeExact(name, value);
    }
    
    private static class SDL_Init {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_Init");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Initialize the SDL library.
     *
     * SDL_Init() simply forwards to calling SDL_InitSubSystem(). Therefore, the
     * two may be used interchangeably. Though for readability of your code
     * SDL_InitSubSystem() might be preferred.
     *
     * The file I/O (for example: SDL_IOFromFile) and threading (SDL_CreateThread)
     * subsystems are initialized by default. Message boxes
     * (SDL_ShowSimpleMessageBox) also attempt to work without initializing the
     * video subsystem, in hopes of being useful in showing an error dialog when
     * SDL_Init fails. You must specifically initialize other subsystems if you
     * use them in your application.
     *
     * Logging (such as SDL_Log) works without initialization, too.
     *
     * `flags` may be any of the following OR'd together:
     *
     * - `SDL_INIT_AUDIO`: audio subsystem; automatically initializes the events
     *   subsystem
     * - `SDL_INIT_VIDEO`: video subsystem; automatically initializes the events
     *   subsystem, should be initialized on the main thread.
     * - `SDL_INIT_JOYSTICK`: joystick subsystem; automatically initializes the
     *   events subsystem
     * - `SDL_INIT_HAPTIC`: haptic (force feedback) subsystem
     * - `SDL_INIT_GAMEPAD`: gamepad subsystem; automatically initializes the
     *   joystick subsystem
     * - `SDL_INIT_EVENTS`: events subsystem
     * - `SDL_INIT_SENSOR`: sensor subsystem; automatically initializes the events
     *   subsystem
     * - `SDL_INIT_CAMERA`: camera subsystem; automatically initializes the events
     *   subsystem
     *
     * Subsystem initialization is ref-counted, you must call SDL_QuitSubSystem()
     * for each SDL_InitSubSystem() to correctly shutdown a subsystem manually (or
     * call SDL_Quit() to force shutdown). If a subsystem is already loaded then
     * this call will increase the ref-count and return.
     *
     * Consider reporting some basic metadata about your application before
     * calling SDL_Init, using either SDL_SetAppMetadata() or
     * SDL_SetAppMetadataProperty().
     *
     * {@snippet lang=c :
     * extern bool SDL_Init(SDL_InitFlags flags)
     * }
     *
     * @param flags subsystem initialization flags.
     * @return true on success or false on failure; call SDL_GetError() for more
     *         information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_SetAppMetadata
     * @see #SDL_SetAppMetadataProperty
     * @see #SDL_InitSubSystem
     * @see #SDL_Quit
     * @see #SDL_SetMainReady
     * @see #SDL_WasInit
     */
    public static boolean SDL_Init(int flags) throws Throwable {
        return (boolean)SDL_Init.HANDLE.invokeExact(flags);
    }

    private static class SDL_Quit {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid();
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_Quit");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Clean up all initialized subsystems.
     *
     * You should call this function even if you have already shutdown each
     * initialized subsystem with SDL_QuitSubSystem(). It is safe to call this
     * function even in the case of errors in initialization.
     *
     * You can use this function with atexit() to ensure that it is run when your
     * application is shutdown, but it is not wise to do this from a library or
     * other dynamically loaded code.
     * 
     * {@snippet lang=c :
     * extern void SDL_Quit()
     * }
     * 
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_Init
     * @see #SDL_QuitSubSystem
     */
    public static void SDL_Quit() throws Throwable {
        SDL_Quit.HANDLE.invokeExact();
    }
    
    private static class SDL_PollEvent {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_PollEvent");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Poll for currently pending events.
     *
     * If `event` is not NULL, the next event is removed from the queue and stored
     * in the SDL_Event structure pointed to by `event`. The 1 returned refers to
     * this event, immediately stored in the SDL Event structure -- not an event
     * to follow.
     *
     * If `event` is NULL, it simply returns 1 if there is an event in the queue,
     * but will not remove it from the queue.
     *
     * As this function may implicitly call SDL_PumpEvents(), you can only call
     * this function in the thread that set the video mode.
     *
     * SDL_PollEvent() is the favored way of receiving system events since it can
     * be done from the main loop and does not suspend the main loop while waiting
     * on an event to be posted.
     *
     * The common practice is to fully process the event queue once every frame,
     * usually as a first step before updating the game's state:
     *
     * {@snippet lang=c :
     * while (game_is_still_running) {
     *     SDL_Event event;
     *     while (SDL_PollEvent(&event)) {  // poll until all events are handled!
     *         // decide what to do with this event.
     *     }
     *
     *     // update game state, draw the current frame
     * }
     * }
     *
     * \threadsafety This should only be run in the thread that initialized the
     *               video subsystem, and for extra safety, you should consider
     *               only doing those things on the main thread in any case.
     * 
     * {@snippet lang=c :
     * extern bool SDL_PollEvent(SDL_Event *event)
     * }
     * 
     * @param event the SDL_Event structure to be filled with the next event from
     *              the queue, or NULL.
     * @return true if this got an event or false if there are none available.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_PushEvent
     * @see #SDL_WaitEvent
     * @see #SDL_WaitEventTimeout
     */
    public static boolean SDL_PollEvent(MemorySegment event) throws Throwable {
        return (boolean)SDL_PollEvent.HANDLE.invokeExact(event);
    }

    private static class SDL_OpenJoystick {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_OpenJoystick");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Open a joystick for use.
     *
     * The joystick subsystem must be initialized before a joystick can be opened
     * for use.
     * 
     * {@snippet lang=c :
     * extern SDL_Joystick *SDL_OpenJoystick(SDL_JoystickID instance_id)
     * })
     *
     * @param instance_id the joystick instance ID.
     * @return a joystick identifier or NULL on failure; call SDL_GetError() for
     *         more information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_CloseJoystick
     */
    public static MemorySegment SDL_OpenJoystick(int instance_id) throws Throwable {
        return (MemorySegment)SDL_OpenJoystick.HANDLE.invokeExact(instance_id);
    }

    private static class SDL_OpenGamepad {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_OpenGamepad");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Open a gamepad for use.
     * 
     * {@snippet lang=c :
     * extern SDL_Gamepad *SDL_OpenGamepad(SDL_JoystickID instance_id)
     * })
     *
     * @param instance_id the joystick instance ID.
     * @return a gamepad identifier or NULL if an error occurred; call
     *         SDL_GetError() for more information.
     * @throws java.lang.Throwable 
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_CloseGamepad
     * @see #SDL_IsGamepad
     */
    public static MemorySegment SDL_OpenGamepad(int instance_id) throws Throwable {
        return (MemorySegment)SDL_OpenGamepad.HANDLE.invokeExact(instance_id);
    }

    private static class SDL_CloseJoystick {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_CloseJoystick");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Close a joystick previously opened with SDL_OpenJoystick().
     * 
     * {@snippet lang=c :
     * extern void SDL_CloseJoystick(SDL_Joystick *joystick)
     * })
     *
     * @param joystick the joystick device to close.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_OpenJoystick
     */
    public static void SDL_CloseJoystick(MemorySegment joystick) throws Throwable {
        SDL_CloseJoystick.HANDLE.invokeExact(joystick);
    }

    private static class SDL_CloseGamepad {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_CloseGamepad");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Close a gamepad previously opened with SDL_OpenGamepad().
     * 
     * {@snippet lang=c :
     * extern void SDL_CloseGamepad(SDL_Gamepad *gamepad)
     * })
     *
     * @param gamepad a gamepad identifier previously returned by
     *                SDL_OpenGamepad().
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_OpenGamepad
     */
    public static void SDL_CloseGamepad(MemorySegment gamepad) throws Throwable {
        SDL_CloseGamepad.HANDLE.invokeExact(gamepad);
    }

    private static class SDL_GetJoystickFromID {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetJoystickFromID");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the SDL_Joystick associated with an instance ID, if it has been opened.
     *
     * {@snippet lang=c :
     * extern SDL_Joystick *SDL_GetJoystickFromID(SDL_JoystickID instance_id)
     * }
     * 
     * @param instance_id the instance ID to get the SDL_Joystick for.
     * @return an SDL_Joystick on success or NULL on failure or if it hasn't been
     *         opened yet; call SDL_GetError() for more information.
     * @throws java.lang.Throwable 
     *
     * @since This function is available since SDL 3.1.3.
     */
    public static MemorySegment SDL_GetJoystickFromID(int instance_id) throws Throwable {
        return (MemorySegment)SDL_GetJoystickFromID.HANDLE.invokeExact(instance_id);
    }

    private static class SDL_GetGamepadFromID {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetGamepadFromID");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the SDL_Gamepad associated with a joystick instance ID, if it has been
     * opened.
     *
     * {@snippet lang=c :
     * extern SDL_Gamepad *SDL_GetGamepadFromID(SDL_JoystickID instance_id)
     * }
     * 
     * @param instance_id the joystick instance ID of the gamepad.
     * @return an SDL_Gamepad on success or NULL on failure or if it hasn't been
     *         opened yet; call SDL_GetError() for more information.
     * @throws java.lang.Throwable 
     *
     * @since This function is available since SDL 3.1.3.
     */
    public static MemorySegment SDL_GetGamepadFromID(int instance_id) throws Throwable {
        return (MemorySegment)SDL_GetGamepadFromID.HANDLE.invokeExact(instance_id);
    }
    
    private static class SDL_GetGamepadTypeForID {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetGamepadTypeForID");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the type of a gamepad.This can be called before any gamepads are opened.
     * 
     *{@snippet lang=c :
     * extern SDL_GamepadType SDL_GetGamepadTypeForID(SDL_JoystickID instance_id)
     * }
     *
     * @param instance_id the joystick instance ID.
     * @return the gamepad type.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetGamepadType
     * @see #SDL_GetGamepads
     * @see #SDL_GetRealGamepadTypeForID
     */
    public static int SDL_GetGamepadTypeForID(int instance_id) throws Throwable {
        return (int)SDL_GetGamepadTypeForID.HANDLE.invokeExact(instance_id);
    }

    private static class SDL_GetGamepadButtonLabel {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetGamepadButtonLabel");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the label of a button on a gamepad.
     * 
     * {@snippet lang=c :
     * extern SDL_GamepadButtonLabel SDL_GetGamepadButtonLabel(SDL_Gamepad *gamepad, SDL_GamepadButton button)
     * })
     * 
     * @param gamepad a gamepad.
     * @param button a button index (one of the SDL_GamepadButton values).
     * @return the SDL_GamepadButtonLabel enum corresponding to the button label.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetGamepadButtonLabelForType
     */
    public static int SDL_GetGamepadButtonLabel(MemorySegment gamepad, int button) throws Throwable {
        return (int)SDL_GetGamepadButtonLabel.HANDLE.invokeExact(gamepad, button);
    }

    private static class SDL_GetJoystickName {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetJoystickName");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the implementation dependent name of a joystick.
     * 
     * {@snippet lang=c :
     * extern const char *SDL_GetJoystickName(SDL_Joystick *joystick)
     * })
     *
     * @param joystick the SDL_Joystick obtained from SDL_OpenJoystick().
     * @return the name of the selected joystick. If no name can be found, this
     *         function returns NULL; call SDL_GetError() for more information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetJoystickNameForID
     */
    public static String SDL_GetJoystickName(MemorySegment joystick) throws Throwable {
        MemorySegment ms = (MemorySegment)SDL_GetJoystickName.HANDLE.invokeExact(joystick);
        return ms.getString(0);
    }

    private static class SDL_GetJoystickPath {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetJoystickPath");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Get the implementation dependent path of a joystick.
     * 
     * {@snippet lang=c :
     * extern const char *SDL_GetJoystickPath(SDL_Joystick *joystick)
     * })
     *
     * @param joystick the SDL_Joystick obtained from SDL_OpenJoystick().
     * @return the path of the selected joystick. If no path can be found, this
     *         function returns NULL; call SDL_GetError() for more information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetJoystickPathForID
     */
    public static String SDL_GetJoystickPath(MemorySegment joystick) throws Throwable {
        MemorySegment ms = (MemorySegment)SDL_GetJoystickPath.HANDLE.invokeExact(joystick);
        if (ms.address() > 0x0)
            return ms.getString(0);
        else
            return "None";
    }
    
    private static class SDL_GetGamepadName {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetGamepadName");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * Get the implementation-dependent name for an opened gamepad.
     * 
     * {@snippet lang=c :
     * extern const char *SDL_GetGamepadName(SDL_Gamepad *gamepad)
     * })
     * 
     * @param gamepad a gamepad identifier previously returned by
     *                SDL_OpenGamepad().
     * @return the implementation dependent name for the gamepad, or NULL if
     *          there is no name or the identifier passed is invalid.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetGamepadNameForID
     */
    public static String SDL_GetGamepadName(MemorySegment gamepad) throws Throwable {
        MemorySegment ms = (MemorySegment)SDL_GetGamepadName.HANDLE.invokeExact(gamepad);
        return ms.getString(0);
    }

    private static class SDL_GetGamepadPath {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetGamepadPath");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * Get the implementation-dependent path for an opened gamepad.
     * 
     * {@snippet lang=c :
     * extern const char *SDL_GetGamepadPath(SDL_Gamepad *gamepad)
     * })
     * 
     * @param gamepad a gamepad identifier previously returned by
     *                SDL_OpenGamepad().
     * @return the implementation dependent path for the gamepad, or NULL if
     *          there is no path or the identifier passed is invalid.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetGamepadPathForID
     */
    public static String SDL_GetGamepadPath(MemorySegment gamepad) throws Throwable {
        MemorySegment ms = (MemorySegment)SDL_GetGamepadPath.HANDLE.invokeExact(gamepad);
        if (ms.address() > 0x0)
            return ms.getString(0);
        else
            return "None";
    }

    private static class SDL_GetError {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GetError");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Retrieve a message about the last error that occurred on the current
     * thread.
     *
     * It is possible for multiple errors to occur before calling SDL_GetError().
     * Only the last error is returned.
     *
     * The message is only applicable when an SDL function has signaled an error.
     * You must check the return values of SDL function calls to determine when to
     * appropriately call SDL_GetError(). You should *not* use the results of
     * SDL_GetError() to decide if an error has occurred! Sometimes SDL will set
     * an error string even when reporting success.
     *
     * SDL will *not* clear the error string for successful API calls. You *must*
     * check return values for failure cases before you can assume the error
     * string applies.
     *
     * Error strings are set per-thread, so an error set in a different thread
     * will not interfere with the current thread's operation.
     *
     * The returned value is a thread-local string which will remain valid until
     * the current thread's error string is changed. The caller should make a copy
     * if the value is needed after the next SDL API call.
     * 
     * \threadsafety It is safe to call this function from any thread.
     * 
     * {@snippet lang=c :
     * extern const char *SDL_GetError()
     * }
     *
     * @return a message with information about the specific error that occurred,
     *         or an empty string if there hasn't been an error message set since
     *         the last call to SDL_ClearError().
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_ClearError
     * @see #SDL_SetError
     */
    public static String SDL_GetError() throws Throwable {
        MemorySegment ms = (MemorySegment)SDL_GetError.HANDLE.invokeExact();
        return ms.getString(0);
    }

    private static class SDL_ClearError {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_ClearError");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Clear any previous error message for this thread.
     * 
     * \threadsafety It is safe to call this function from any thread.
     * 
     * {@snippet lang=c :
     * extern bool SDL_ClearError()
     * })
     * 
     * @return true.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetError
     * @see #SDL_SetError
     */
    public static boolean SDL_ClearError() throws Throwable {
        return (boolean)SDL_ClearError.HANDLE.invokeExact();
    }
    
    private static class SDL_GamepadHasSensor {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL, C_POINTER, C_INT);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_GamepadHasSensor");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Return whether a gamepad has a particular sensor.
     * 
     * {@snippet lang=c :
     * extern bool SDL_GamepadHasSensor(SDL_Gamepad *gamepad, SDL_SensorType type)
     * })
     * 
     * @param gamepad the gamepad to query.
     * @param type the type of sensor to query.
     * @return true if the sensor exists, false otherwise.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GetGamepadSensorData
     * @see #SDL_GetGamepadSensorDataRate
     * @see #SDL_SetGamepadSensorEnabled
     */
    public static boolean SDL_GamepadHasSensor(MemorySegment gamepad, int type) throws Throwable {
        return (boolean)SDL_GamepadHasSensor.HANDLE.invokeExact(gamepad, type);
    }
    
    private static class SDL_SetGamepadSensorEnabled {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_BOOL, C_POINTER, C_INT, C_BOOL);
        public static final MemorySegment ADDR = Gamepad.findOrThrow("SDL_SetGamepadSensorEnabled");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * Set whether data reporting for a gamepad sensor is enabled.
     *
     * {@snippet lang=c :
     * extern bool SDL_SetGamepadSensorEnabled(SDL_Gamepad *gamepad, SDL_SensorType type, bool enabled)
     * }
     * 
     * @param gamepad the gamepad to update.
     * @param type the type of sensor to enable/disable.
     * @param enabled whether data reporting should be enabled.
     * @return true on success or false on failure; call SDL_GetError() for more
     *          information.
     * @throws java.lang.Throwable
     *
     * @since This function is available since SDL 3.1.3.
     *
     * @see #SDL_GamepadHasSensor
     * @see #SDL_GamepadSensorEnabled
     */
    public static boolean SDL_SetGamepadSensorEnabled(MemorySegment gamepad, int type, boolean enabled) throws Throwable {
        return (boolean)SDL_SetGamepadSensorEnabled.HANDLE.invokeExact(gamepad, type, enabled);
    }
}
