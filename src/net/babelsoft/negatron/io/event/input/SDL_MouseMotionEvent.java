// Generated by jextract

package net.babelsoft.negatron.io.event.input;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import net.babelsoft.negatron.io.Gamepad;

/**
 * {@snippet lang=c :
 * struct SDL_MouseMotionEvent {
 *     SDL_EventType type;
 *     Uint32 reserved;
 *     Uint64 timestamp;
 *     SDL_WindowID windowID;
 *     SDL_MouseID which;
 *     SDL_MouseButtonFlags state;
 *     float x;
 *     float y;
 *     float xrel;
 *     float yrel;
 * }
 * }
 */
public class SDL_MouseMotionEvent {

    SDL_MouseMotionEvent() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(Gamepad.C_INT.withName("type"),
        Gamepad.C_INT.withName("reserved"),
        Gamepad.C_LONG_LONG.withName("timestamp"),
        Gamepad.C_INT.withName("windowID"),
        Gamepad.C_INT.withName("which"),
        Gamepad.C_INT.withName("state"),
        Gamepad.C_FLOAT.withName("x"),
        Gamepad.C_FLOAT.withName("y"),
        Gamepad.C_FLOAT.withName("xrel"),
        Gamepad.C_FLOAT.withName("yrel"),
        MemoryLayout.paddingLayout(4)
    ).withName("SDL_MouseMotionEvent");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt type$LAYOUT = (OfInt)$LAYOUT.select(groupElement("type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * SDL_EventType type
     * }
     */
    public static final OfInt type$layout() {
        return type$LAYOUT;
    }

    private static final long type$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * SDL_EventType type
     * }
     */
    public static final long type$offset() {
        return type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * SDL_EventType type
     * }
     */
    public static int type(MemorySegment struct) {
        return struct.get(type$LAYOUT, type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * SDL_EventType type
     * }
     */
    public static void type(MemorySegment struct, int fieldValue) {
        struct.set(type$LAYOUT, type$OFFSET, fieldValue);
    }

    private static final OfInt reserved$LAYOUT = (OfInt)$LAYOUT.select(groupElement("reserved"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Uint32 reserved
     * }
     */
    public static final OfInt reserved$layout() {
        return reserved$LAYOUT;
    }

    private static final long reserved$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Uint32 reserved
     * }
     */
    public static final long reserved$offset() {
        return reserved$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Uint32 reserved
     * }
     */
    public static int reserved(MemorySegment struct) {
        return struct.get(reserved$LAYOUT, reserved$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Uint32 reserved
     * }
     */
    public static void reserved(MemorySegment struct, int fieldValue) {
        struct.set(reserved$LAYOUT, reserved$OFFSET, fieldValue);
    }

    private static final OfLong timestamp$LAYOUT = (OfLong)$LAYOUT.select(groupElement("timestamp"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Uint64 timestamp
     * }
     */
    public static final OfLong timestamp$layout() {
        return timestamp$LAYOUT;
    }

    private static final long timestamp$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Uint64 timestamp
     * }
     */
    public static final long timestamp$offset() {
        return timestamp$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Uint64 timestamp
     * }
     */
    public static long timestamp(MemorySegment struct) {
        return struct.get(timestamp$LAYOUT, timestamp$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Uint64 timestamp
     * }
     */
    public static void timestamp(MemorySegment struct, long fieldValue) {
        struct.set(timestamp$LAYOUT, timestamp$OFFSET, fieldValue);
    }

    private static final OfInt windowID$LAYOUT = (OfInt)$LAYOUT.select(groupElement("windowID"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * SDL_WindowID windowID
     * }
     */
    public static final OfInt windowID$layout() {
        return windowID$LAYOUT;
    }

    private static final long windowID$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * SDL_WindowID windowID
     * }
     */
    public static final long windowID$offset() {
        return windowID$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * SDL_WindowID windowID
     * }
     */
    public static int windowID(MemorySegment struct) {
        return struct.get(windowID$LAYOUT, windowID$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * SDL_WindowID windowID
     * }
     */
    public static void windowID(MemorySegment struct, int fieldValue) {
        struct.set(windowID$LAYOUT, windowID$OFFSET, fieldValue);
    }

    private static final OfInt which$LAYOUT = (OfInt)$LAYOUT.select(groupElement("which"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * SDL_MouseID which
     * }
     */
    public static final OfInt which$layout() {
        return which$LAYOUT;
    }

    private static final long which$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * SDL_MouseID which
     * }
     */
    public static final long which$offset() {
        return which$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * SDL_MouseID which
     * }
     */
    public static int which(MemorySegment struct) {
        return struct.get(which$LAYOUT, which$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * SDL_MouseID which
     * }
     */
    public static void which(MemorySegment struct, int fieldValue) {
        struct.set(which$LAYOUT, which$OFFSET, fieldValue);
    }

    private static final OfInt state$LAYOUT = (OfInt)$LAYOUT.select(groupElement("state"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * SDL_MouseButtonFlags state
     * }
     */
    public static final OfInt state$layout() {
        return state$LAYOUT;
    }

    private static final long state$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * SDL_MouseButtonFlags state
     * }
     */
    public static final long state$offset() {
        return state$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * SDL_MouseButtonFlags state
     * }
     */
    public static int state(MemorySegment struct) {
        return struct.get(state$LAYOUT, state$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * SDL_MouseButtonFlags state
     * }
     */
    public static void state(MemorySegment struct, int fieldValue) {
        struct.set(state$LAYOUT, state$OFFSET, fieldValue);
    }

    private static final OfFloat x$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("x"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float x
     * }
     */
    public static final OfFloat x$layout() {
        return x$LAYOUT;
    }

    private static final long x$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float x
     * }
     */
    public static final long x$offset() {
        return x$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float x
     * }
     */
    public static float x(MemorySegment struct) {
        return struct.get(x$LAYOUT, x$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float x
     * }
     */
    public static void x(MemorySegment struct, float fieldValue) {
        struct.set(x$LAYOUT, x$OFFSET, fieldValue);
    }

    private static final OfFloat y$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("y"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float y
     * }
     */
    public static final OfFloat y$layout() {
        return y$LAYOUT;
    }

    private static final long y$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float y
     * }
     */
    public static final long y$offset() {
        return y$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float y
     * }
     */
    public static float y(MemorySegment struct) {
        return struct.get(y$LAYOUT, y$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float y
     * }
     */
    public static void y(MemorySegment struct, float fieldValue) {
        struct.set(y$LAYOUT, y$OFFSET, fieldValue);
    }

    private static final OfFloat xrel$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("xrel"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float xrel
     * }
     */
    public static final OfFloat xrel$layout() {
        return xrel$LAYOUT;
    }

    private static final long xrel$OFFSET = 36;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float xrel
     * }
     */
    public static final long xrel$offset() {
        return xrel$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float xrel
     * }
     */
    public static float xrel(MemorySegment struct) {
        return struct.get(xrel$LAYOUT, xrel$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float xrel
     * }
     */
    public static void xrel(MemorySegment struct, float fieldValue) {
        struct.set(xrel$LAYOUT, xrel$OFFSET, fieldValue);
    }

    private static final OfFloat yrel$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("yrel"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float yrel
     * }
     */
    public static final OfFloat yrel$layout() {
        return yrel$LAYOUT;
    }

    private static final long yrel$OFFSET = 40;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float yrel
     * }
     */
    public static final long yrel$offset() {
        return yrel$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float yrel
     * }
     */
    public static float yrel(MemorySegment struct) {
        return struct.get(yrel$LAYOUT, yrel$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float yrel
     * }
     */
    public static void yrel(MemorySegment struct, float fieldValue) {
        struct.set(yrel$LAYOUT, yrel$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}
