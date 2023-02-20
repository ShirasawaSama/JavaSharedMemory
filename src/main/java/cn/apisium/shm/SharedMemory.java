package cn.apisium.shm;

import cn.apisium.shm.impl.WindowsSharedMemory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * The shared memory.
 */
@SuppressWarnings("unused")
public interface SharedMemory extends AutoCloseable {
    /**
     * Get the shared memory as a {@link ByteBuffer}.
     * @return The shared memory as a {@link ByteBuffer}.
     */
    @NotNull
    @Contract(pure = true)
    ByteBuffer toByteBuffer();

    /**
     * Get the shared memory as a {@link MemorySegment}.
     * @return The shared memory as a {@link MemorySegment}.
     */
    @NotNull
    @Contract(pure = true)
    MemorySegment getMemorySegment();

    /**
     * Get the size of the shared memory.
     * @return The size of the shared memory.
     */
    @Contract(pure = true)
    int size();

    /**
     * Get the name of the shared memory.
     * @return The name of the shared memory.
     */
    @NotNull
    @Contract(pure = true)
    String getName();

    /**
     * Open an exists shared memory.
     * @param name The name of the shared memory.
     * @param size The size of the shared memory.
     * @return The shared memory.
     */
    @NotNull
    static SharedMemory open(@NotNull String name, int size) {
        return init(name, size, false);
    }

    /**
     * Create a new shared memory.
     * @param name The name of the shared memory.
     * @param size The size of the shared memory.
     * @return The shared memory.
     */
    @NotNull
    static SharedMemory create(@NotNull String name, int size) {
        return init(name, size, true);
    }

    /**
     * Check if the current system is supported.
     * @return If the current system is supported.
     */
    @Contract(pure = true)
    static boolean isSupported() {
        return CABI.SYSTEM_TYPE == CABI.SystemType.Windows;
    }

    @NotNull
    private static SharedMemory init(@NotNull String name, int size, boolean isCreate) {
        try {
            return switch (CABI.SYSTEM_TYPE) {
                case Windows -> new WindowsSharedMemory(name, size, isCreate);
                case Linux -> throw new UnsupportedOperationException("Linux is not supported yet");
                case MacOS -> throw new UnsupportedOperationException("MacOS is not supported yet");
                default -> throw new UnsupportedOperationException("Only Windows, Linux and MacOS are supported");
            };
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }
}
