package cn.apisium.shm;

import cn.apisium.shm.impl.WindowsSharedMemory;

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
    ByteBuffer toByteBuffer();

    /**
     * Get the size of the shared memory.
     * @return The size of the shared memory.
     */
    int size();

    /**
     * Get the name of the shared memory.
     * @return The name of the shared memory.
     */
    String getName();

    /**
     * Open an exists shared memory.
     * @param name The name of the shared memory.
     * @param size The size of the shared memory.
     * @return The shared memory.
     */
    static SharedMemory open(String name, int size) {
        return init(name, size, false);
    }

    /**
     * Create a new shared memory.
     * @param name The name of the shared memory.
     * @param size The size of the shared memory.
     * @return The shared memory.
     */
    static SharedMemory create(String name, int size) {
        return init(name, size, true);
    }

    private static SharedMemory init(String name, int size, boolean isCreate) {
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
