package cn.apisium.shm;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public abstract class AbstractSharedMemory implements SharedMemory {
    private final Arena arena = Arena.ofShared();
    private final int size;
    private final String name;
    protected final boolean isCreate;

    protected MemorySegment segment;

    public AbstractSharedMemory(String name, int size) { this(name, size, true); }
    public AbstractSharedMemory(String name, int size, boolean isCreate) {
        this.name = name;
        this.size = size;
        this.isCreate = isCreate;
    }

    @Override
    public @NotNull MemorySegment getMemorySegment() { return segment; }

    @Override
    public @NotNull ByteBuffer toByteBuffer() { return segment.asByteBuffer().order(ByteOrder.nativeOrder()); }

    @Override
    public int getSize() { return size; }

    @Override
    public @NotNull String getName() { return name; }
}
