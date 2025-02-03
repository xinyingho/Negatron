/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2025 BabelSoft S.A.S.U.
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
 * @author xinyingho
 */
public class Mouse {
    
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
            File libSDL = new File(appPath.toString()+ "/" + System.mapLibraryName("evdev"));
            System.load(libSDL.getAbsolutePath());
            isLoaded = true;
        } catch (UnsatisfiedLinkError e) { }
        
        if (!isLoaded) try {
            File libSDL = new File("./" + System.mapLibraryName("evdev"));
            System.load(libSDL.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            System.loadLibrary("evdev");
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
    
    public class LibevdevUinputOpenMode {
        /* intentionally -2 to avoid code like below from accidentally working:
            fd = open("/dev/uinput", O_RDWR); // fails, fd is -1
            libevdev_uinput_create_from_device(dev, fd, &uidev); // may hide the error */
        public static final int LIBEVDEV_UINPUT_OPEN_MANAGED = (int)-2L; /**< let libevdev open and close @c /dev/uinput */
    }

    /*
    * Device properties and quirks
    */
    public class DeviceProperty { 
        public static final int INPUT_PROP_POINTER		= 0x00;	/* needs a pointer */
        public static final int INPUT_PROP_DIRECT		= 0x01;	/* direct input devices */
        public static final int INPUT_PROP_BUTTONPAD		= 0x02;	/* has button(s) under pad */
        public static final int INPUT_PROP_SEMI_MT		= 0x03;	/* touch rectangle only */
        public static final int INPUT_PROP_TOPBUTTONPAD		= 0x04;	/* softbuttons at top of pad */
        public static final int INPUT_PROP_POINTING_STICK	= 0x05;	/* is a pointing stick */
        public static final int INPUT_PROP_ACCELEROMETER        = 0x06;	/* has accelerometer */

        public static final int INPUT_PROP_MAX			= 0x1f;
        public static final int INPUT_PROP_CNT			= INPUT_PROP_MAX + 1;
    }
    
    public class EventType {
        public static final int EV_SYN          = 0x00;
        public static final int EV_KEY          = 0x01;
        public static final int EV_REL          = 0x02;
        public static final int EV_ABS          = 0x03;
        public static final int EV_MSC          = 0x04;
        public static final int EV_SW           = 0x05;
        public static final int EV_LED          = 0x11;
        public static final int EV_SND          = 0x12;
        public static final int EV_REP          = 0x14;
        public static final int EV_FF           = 0x15;
        public static final int EV_PWR          = 0x16;
        public static final int EV_FF_STATUS    = 0x17;
        public static final int EV_MAX          = 0x1F;
        public static final int EV_CNT          = EV_MAX + 1;
    }
    
    public class SynchronizationEvent {
        public static final int SYN_REPORT      = 0;
        public static final int SYN_CONFIG      = 1;
        public static final int SYN_MT_REPORT   = 2;
        public static final int SYN_DROPPED     = 3;
        public static final int SYN_MAX         = 0xF;
        public static final int SYN_CNT         = SYN_MAX + 1;
    }
    
    public class RelativeAxe {
        public static final int REL_X           = 0x00;
        public static final int REL_Y           = 0x01;
        public static final int REL_Z           = 0x02;
        public static final int REL_RX          = 0x03;
        public static final int REL_RY          = 0x04;
        public static final int REL_RZ          = 0x05;
        public static final int REL_HWHEEL      = 0x06;
        public static final int REL_DIAL        = 0x07;
        public static final int REL_WHEEL       = 0x08;
        public static final int REL_MISC        = 0x09;
        /*
         * = 0x0a is reserved and should not be used in input drivers.
         * It was used by HID as REL_MISC+1 and userspace needs to detect if
         * the next REL_* event is correct or is just REL_MISC + n.
         * We define here REL_RESERVED so userspace can rely on it and detect
         * the situation described above.
         */
        public static final int REL_RESERVED        = 0x0A;
        public static final int REL_WHEEL_HI_RES    = 0x0B;
        public static final int REL_HWHEEL_HI_RES   = 0x0C;
        public static final int REL_MAX             = 0x0F;
        public static final int REL_CNT             = REL_MAX + 1;
    }
    
    public class AbsoluteAxe {
        public static final int ABS_X                   = 0x00;
        public static final int ABS_Y                   = 0x01;
        public static final int ABS_Z                   = 0x02;
        public static final int ABS_RX                  = 0x03;
        public static final int ABS_RY                  = 0x04;
        public static final int ABS_RZ                  = 0x05;
        public static final int ABS_THROTTLE            = 0x06;
        public static final int ABS_RUDDER		= 0x07;
        public static final int ABS_WHEEL		= 0x08;
        public static final int ABS_GAS			= 0x09;
        public static final int ABS_BRAKE		= 0x0A;
        public static final int ABS_HAT0X		= 0x10;
        public static final int ABS_HAT0Y		= 0x11;
        public static final int ABS_HAT1X		= 0x12;
        public static final int ABS_HAT1Y		= 0x13;
        public static final int ABS_HAT2X		= 0x14;
        public static final int ABS_HAT2Y		= 0x15;
        public static final int ABS_HAT3X		= 0x16;
        public static final int ABS_HAT3Y		= 0x17;
        public static final int ABS_PRESSURE		= 0x18;
        public static final int ABS_DISTANCE		= 0x19;
        public static final int ABS_TILT_X		= 0x1A;
        public static final int ABS_TILT_Y		= 0x1B;
        public static final int ABS_TOOL_WIDTH		= 0x1C;

        public static final int ABS_VOLUME		= 0x20;
        public static final int ABS_PROFILE		= 0x21;

        public static final int ABS_MISC                 = 0x28;

        /*
         * = 0x2e is reserved and should not be used in input drivers.
         * It was used by HID as ABS_MISC+6 and userspace needs to detect if
         * the next ABS_* event is correct or is just ABS_MISC + n.
         * We define here ABS_RESERVED so userspace can rely on it and detect
         * the situation described above.
         */
        public static final int ABS_RESERVED		= 0x2E;

        public static final int ABS_MT_SLOT		= 0x2F;	/* MT slot being modified */
        public static final int ABS_MT_TOUCH_MAJOR	= 0x30;	/* Major axis of touching ellipse */
        public static final int ABS_MT_TOUCH_MINOR	= 0x31;	/* Minor axis (omit if circular) */
        public static final int ABS_MT_WIDTH_MAJOR	= 0x32;	/* Major axis of approaching ellipse */
        public static final int ABS_MT_WIDTH_MINOR	= 0x33;	/* Minor axis (omit if circular) */
        public static final int ABS_MT_ORIENTATION	= 0x34;	/* Ellipse orientation */
        public static final int ABS_MT_POSITION_X	= 0x35;	/* Center X touch position */
        public static final int ABS_MT_POSITION_Y	= 0x36;	/* Center Y touch position */
        public static final int ABS_MT_TOOL_TYPE        = 0x37;	/* Type of touching device */
        public static final int ABS_MT_BLOB_ID		= 0x38;	/* Group a set of packets as a blob */
        public static final int ABS_MT_TRACKING_ID	= 0x39;	/* Unique ID of initiated contact */
        public static final int ABS_MT_PRESSURE		= 0x3A;	/* Pressure on contact area */
        public static final int ABS_MT_DISTANCE		= 0x3B;	/* Contact hover distance */
        public static final int ABS_MT_TOOL_X		= 0x3C;	/* Center X tool position */
        public static final int ABS_MT_TOOL_Y		= 0x3D;	/* Center Y tool position */


        public static final int ABS_MAX			= 0x3F;
        public static final int ABS_CNT			= ABS_MAX + 1;
    }
    
    public class Button {
        public static final int BTN_MOUSE		= 0x110;
        public static final int BTN_LEFT                = 0x110;
        public static final int BTN_RIGHT		= 0x111;
        public static final int BTN_MIDDLE		= 0x112;
        public static final int BTN_SIDE                = 0x113;
        public static final int BTN_EXTRA		= 0x114;
        public static final int BTN_FORWARD		= 0x115;
        public static final int BTN_BACK                = 0x116;
        public static final int BTN_TASK                = 0x117;
    }
    
    private static class libevdev_new {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_new");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup init
     *
     * Initialize a new libevdev device. This function only allocates the
     * required memory and initializes the struct to sane default values.
     * To actually hook up the device to a kernel device, use
     * libevdev_set_fd().
     *
     * Memory allocated through libevdev_new() must be released by the
     * caller with libevdev_free().
     * 
     * {@snippet lang=c :
     * struct libevdev *libevdev_new()
     * }
     * 
     * @throws java.lang.Throwable
     *
     * @see #libevdev_set_fd
     * @see #libevdev_free
     */
    public static MemorySegment libevdev_new() throws Throwable {
        return (MemorySegment)libevdev_new.HANDLE.invokeExact();
    }
    
    private static class libevdev_set_name {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(C_POINTER, C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_set_name");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup kernel
     *
     * Change the device's name as returned by libevdev_get_name(). This
     * function destroys the string previously returned by libevdev_get_name(),
     * a caller must take care that no references are kept.
     * 
     * {@snippet lang=c :
     * void libevdev_set_name(struct libevdev *dev, const char *name)
     * }
     *
     * @param dev The evdev device
     * @param name The new, non-NULL, name to assign to this device.
     * @throws java.lang.Throwable
     *
     * @note This function may be called before libevdev_set_fd(). A call to
     * libevdev_set_fd() will overwrite any previously set value.
     */
    public static void libevdev_set_name(MemorySegment dev, MemorySegment name) throws Throwable {
        libevdev_set_name.HANDLE.invokeExact(dev, name);
    }

    private static class libevdev_enable_event_type {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_enable_event_type");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup kernel
     *
     * Forcibly enable an event type on this device, even if the underlying
     * device does not support it. While this cannot make the device actually
     * report such events, it will now return true for libevdev_has_event_type().
     *
     * This is a local modification only affecting only this representation of
     * this device.
     * 
     * {@snippet lang=c :
     * int libevdev_enable_event_type(struct libevdev *dev, unsigned int type)
     * }
     *
     * @param dev The evdev device, already initialized with libevdev_set_fd()
     * @param type The event type to enable (EV_ABS, EV_KEY, ...)
     *
     * @return 0 on success or -1 otherwise
     * @throws java.lang.Throwable
     *
     * @see #libevdev_has_event_type
     */
    public static int libevdev_enable_event_type(MemorySegment dev, int type) throws Throwable {
        return (int)libevdev_enable_event_type.HANDLE.invokeExact(dev, type);
    }

    private static class libevdev_enable_event_code {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_enable_event_code");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * @ingroup kernel
     *
     * Forcibly enable an event code on this device, even if the underlying
     * device does not support it. While this cannot make the device actually
     * report such events, it will now return true for libevdev_has_event_code().
     *
     * The last argument depends on the type and code:
     * - If type is EV_ABS, data must be a pointer to a struct input_absinfo
     *   containing the data for this axis.
     * - If type is EV_REP, data must be a pointer to a int containing the data
     *   for this axis
     * - For all other types, the argument must be NULL.
     *
     * This function calls libevdev_enable_event_type() if necessary.
     *
     * This is a local modification only affecting only this representation of
     * this device.
     *
     * If this function is called with a type of EV_ABS and EV_REP on a device
     * that already has the given event code enabled, the values in data
     * overwrite the previous values.
     * 
     * {@snippet lang=c :
     * int libevdev_enable_event_code(struct libevdev *dev, unsigned int type, unsigned int code, const void *data)
     * }
     *
     * @param dev The evdev device, already initialized with libevdev_set_fd()
     * @param type The event type to enable (EV_ABS, EV_KEY, ...)
     * @param code The event code to enable (ABS_X, REL_X, etc.)
     * @param data If type is EV_ABS, data points to a struct input_absinfo. If type is EV_REP, data
     * points to an integer. Otherwise, data must be NULL.
     *
     * @return 0 on success or -1 otherwise
     * @throws java.lang.Throwable
     *
     * @see #libevdev_enable_event_type
     */
    public static int libevdev_enable_event_code(MemorySegment dev, int type, int code, MemorySegment data) throws Throwable {
        return (int)libevdev_enable_event_code.HANDLE.invokeExact(dev, type, code, data);
    }

    private static class libevdev_uinput_create_from_device {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_uinput_create_from_device");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * @ingroup uinput
     *
     * Create a uinput device based on the given libevdev device. The uinput device
     * will be an exact copy of the libevdev device, minus the bits that uinput doesn't
     * allow to be set.
     *
     * If uinput_fd is @ref LIBEVDEV_UINPUT_OPEN_MANAGED, libevdev_uinput_create_from_device()
     * will open @c /dev/uinput in read/write mode and manage the file descriptor.
     * Otherwise, uinput_fd must be opened by the caller and opened with the
     * appropriate permissions.
     *
     * The device's lifetime is tied to the uinput file descriptor, closing it will
     * destroy the uinput device. You should call libevdev_uinput_destroy() before
     * closing the file descriptor to free allocated resources.
     * A file descriptor can only create one uinput device at a time; the second device
     * will fail with -EINVAL.
     *
     * You don't need to keep the file descriptor variable around,
     * libevdev_uinput_get_fd() will return it when needed.
     *
     * @note Due to limitations in the uinput kernel module, REP_DELAY and
     * REP_PERIOD will default to the kernel defaults, not to the ones set in the
     * source device.
     *
     * @note On FreeBSD, if the UI_GET_SYSNAME ioctl() fails, there is no other way
     * to get a device, and the function call will fail.
     * 
     * {@snippet lang=c :
     * int libevdev_uinput_create_from_device(const struct libevdev *dev, int uinput_fd, struct libevdev_uinput **uinput_dev)
     * }
     *
     * @param dev The device to duplicate
     * @param uinput_fd @ref LIBEVDEV_UINPUT_OPEN_MANAGED or a file descriptor to @c /dev/uinput,
     * @param[out] uinput_dev The newly created libevdev device.
     *
     * @return 0 on success or a negative errno on failure. On failure, the value of
     * uinput_dev is unmodified.
     * @throws java.lang.Throwable
     *
     * @see #libevdev_uinput_destroy
     */
    public static int libevdev_uinput_create_from_device(MemorySegment dev, int uinput_fd, MemorySegment uinput_dev) throws Throwable {
        return (int)libevdev_uinput_create_from_device.HANDLE.invokeExact(dev, uinput_fd, uinput_dev);
    }

    private static class libevdev_uinput_destroy {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_uinput_destroy");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * @ingroup uinput
     *
     * Destroy a previously created uinput device and free associated memory.
     *
     * If the device was opened with @ref LIBEVDEV_UINPUT_OPEN_MANAGED,
     * libevdev_uinput_destroy() also closes the file descriptor. Otherwise, the
     * fd is left as-is and must be closed by the caller.
     *
     * {@snippet lang=c :
     * void libevdev_uinput_destroy(struct libevdev_uinput *uinput_dev)
     * }
     * 
     * @param uinput_dev A previously created uinput device.
     * @throws java.lang.Throwable
     */
    public static void libevdev_uinput_destroy(MemorySegment uinput_dev) throws Throwable {
        libevdev_uinput_destroy.HANDLE.invokeExact(uinput_dev);
    }

    private static class libevdev_free {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(C_POINTER);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_free");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup init
     *
     * Clean up and free the libevdev struct. After completion, the <code>struct
     * libevdev</code> is invalid and must not be used.
     *
     * Note that calling libevdev_free() does not close the file descriptor
     * currently associated with this instance.
     *
     * {@snippet lang=c :
     * void libevdev_free(struct libevdev *dev)
     * }
     * 
     * @param dev The evdev device
     * @throws java.lang.Throwable
     *
     * @note This function may be called before libevdev_set_fd().
     */
    public static void libevdev_free(MemorySegment dev) throws Throwable {
        libevdev_free.HANDLE.invokeExact(dev);
    }

    private static class libevdev_uinput_write_event {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_INT);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_uinput_write_event");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup uinput
     *
     * Post an event through the uinput device. It is the caller's responsibility
     * that any event sequence is terminated with an EV_SYN/SYN_REPORT/0 event.
     * Otherwise, listeners on the device node will not see the events until the
     * next EV_SYN event is posted.
     *
     * {@snippet lang=c :
     * int libevdev_uinput_write_event(const struct libevdev_uinput *uinput_dev, unsigned int type, unsigned int code, int value)
     * }
     * 
     * @param uinput_dev A previously created uinput device.
     * @param type Event type (EV_ABS, EV_REL, etc.)
     * @param code Event code (ABS_X, REL_Y, etc.)
     * @param value The event value
     * @return 0 on success or a negative errno on error
     * @throws java.lang.Throwable
     */
    public static int libevdev_uinput_write_event(MemorySegment uinput_dev, int type, int code, int value) throws Throwable {
        return (int)libevdev_uinput_write_event.HANDLE.invokeExact(uinput_dev, type, code, value);
    }

    private static class libevdev_enable_property {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_INT, C_POINTER, C_INT);
        public static final MemorySegment ADDR = Mouse.findOrThrow("libevdev_enable_property");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    /**
     * @ingroup kernel
     *
     * {@snippet lang=c :
     * int libevdev_enable_property(struct libevdev *dev, unsigned int prop)
     * }
     * 
     * @param dev The evdev device
     * @param prop The input property to enable, one of INPUT_PROP_...
     *
     * @return 0 on success or -1 on failure
     * @throws java.lang.Throwable
     *
     * @note This function may be called before libevdev_set_fd(). A call to
     * libevdev_set_fd() will overwrite any previously set value.
     */
    public static int libevdev_enable_property(MemorySegment dev, int prop) throws Throwable {
        return (int)libevdev_enable_property.HANDLE.invokeExact(dev, prop);
    }

    private static class strerror {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(C_POINTER, C_INT);
        public static final MemorySegment ADDR = Mouse.findOrThrow("strerror");
        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }
    
    public static String strerror(int errno) throws Throwable {
        MemorySegment str = (MemorySegment) strerror.HANDLE.invokeExact(errno);
        return str.getString(0);
    }
}
