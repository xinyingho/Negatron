// Generated by jextract

package net.babelsoft.negatron.io.event;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import net.babelsoft.negatron.io.Gamepad;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct SDL_AudioDeviceEvent {
 *     SDL_EventType type;
 *     Uint32 reserved;
 *     Uint64 timestamp;
 *     SDL_AudioDeviceID which;
 *     bool recording;
 *     Uint8 padding1;
 *     Uint8 padding2;
 *     Uint8 padding3;
 * }
 * }
 */
public class SDL_AudioDeviceEvent {

    SDL_AudioDeviceEvent() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(Gamepad.C_INT.withName("type"),
        Gamepad.C_INT.withName("reserved"),
        Gamepad.C_LONG_LONG.withName("timestamp"),
        Gamepad.C_INT.withName("which"),
        Gamepad.C_BOOL.withName("recording"),
        Gamepad.C_CHAR.withName("padding1"),
        Gamepad.C_CHAR.withName("padding2"),
        Gamepad.C_CHAR.withName("padding3")
    ).withName("SDL_AudioDeviceEvent");

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

    private static final OfInt which$LAYOUT = (OfInt)$LAYOUT.select(groupElement("which"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * SDL_AudioDeviceID which
     * }
     */
    public static final OfInt which$layout() {
        return which$LAYOUT;
    }

    private static final long which$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * SDL_AudioDeviceID which
     * }
     */
    public static final long which$offset() {
        return which$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * SDL_AudioDeviceID which
     * }
     */
    public static int which(MemorySegment struct) {
        return struct.get(which$LAYOUT, which$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * SDL_AudioDeviceID which
     * }
     */
    public static void which(MemorySegment struct, int fieldValue) {
        struct.set(which$LAYOUT, which$OFFSET, fieldValue);
    }

    private static final OfBoolean recording$LAYOUT = (OfBoolean)$LAYOUT.select(groupElement("recording"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * bool recording
     * }
     */
    public static final OfBoolean recording$layout() {
        return recording$LAYOUT;
    }

    private static final long recording$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * bool recording
     * }
     */
    public static final long recording$offset() {
        return recording$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * bool recording
     * }
     */
    public static boolean recording(MemorySegment struct) {
        return struct.get(recording$LAYOUT, recording$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * bool recording
     * }
     */
    public static void recording(MemorySegment struct, boolean fieldValue) {
        struct.set(recording$LAYOUT, recording$OFFSET, fieldValue);
    }

    private static final OfByte padding1$LAYOUT = (OfByte)$LAYOUT.select(groupElement("padding1"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Uint8 padding1
     * }
     */
    public static final OfByte padding1$layout() {
        return padding1$LAYOUT;
    }

    private static final long padding1$OFFSET = 21;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Uint8 padding1
     * }
     */
    public static final long padding1$offset() {
        return padding1$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Uint8 padding1
     * }
     */
    public static byte padding1(MemorySegment struct) {
        return struct.get(padding1$LAYOUT, padding1$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Uint8 padding1
     * }
     */
    public static void padding1(MemorySegment struct, byte fieldValue) {
        struct.set(padding1$LAYOUT, padding1$OFFSET, fieldValue);
    }

    private static final OfByte padding2$LAYOUT = (OfByte)$LAYOUT.select(groupElement("padding2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Uint8 padding2
     * }
     */
    public static final OfByte padding2$layout() {
        return padding2$LAYOUT;
    }

    private static final long padding2$OFFSET = 22;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Uint8 padding2
     * }
     */
    public static final long padding2$offset() {
        return padding2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Uint8 padding2
     * }
     */
    public static byte padding2(MemorySegment struct) {
        return struct.get(padding2$LAYOUT, padding2$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Uint8 padding2
     * }
     */
    public static void padding2(MemorySegment struct, byte fieldValue) {
        struct.set(padding2$LAYOUT, padding2$OFFSET, fieldValue);
    }

    private static final OfByte padding3$LAYOUT = (OfByte)$LAYOUT.select(groupElement("padding3"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Uint8 padding3
     * }
     */
    public static final OfByte padding3$layout() {
        return padding3$LAYOUT;
    }

    private static final long padding3$OFFSET = 23;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Uint8 padding3
     * }
     */
    public static final long padding3$offset() {
        return padding3$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Uint8 padding3
     * }
     */
    public static byte padding3(MemorySegment struct) {
        return struct.get(padding3$LAYOUT, padding3$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Uint8 padding3
     * }
     */
    public static void padding3(MemorySegment struct, byte fieldValue) {
        struct.set(padding3$LAYOUT, padding3$OFFSET, fieldValue);
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

