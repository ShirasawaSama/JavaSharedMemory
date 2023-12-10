package cn.apisium.shm.impl;

import cn.apisium.shm.AbstractSharedMemory;
import cn.apisium.shm.CABI;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class MmapSharedMemory extends AbstractSharedMemory {
    private static MethodHandle shm_open, ftruncate, mmap, munmap, shm_unlink;
    private static final int O_CREAT = 0x00000200, O_EXCL = 0x00000800, O_RDWR = 0x0002,
            PROT_READ = 0x01, PROT_WRITE = 0x02, MAP_SHARED = 0x01;
    @SuppressWarnings("OctalInteger")
    private static final short S_IRUSR = 00400, S_IWUSR = 00200;

    static {
        if (CABI.SYSTEM_TYPE == CABI.SystemType.Unix) {
            var linker = Linker.nativeLinker();
            var lookup = linker.defaultLookup();
            shm_open = linker.downcallHandle(lookup.find("shm_open").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ), Linker.Option.firstVariadicArg(2));
            ftruncate = linker.downcallHandle(lookup.find("ftruncate").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ));
            mmap = linker.downcallHandle(lookup.find("mmap").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ));
            munmap = linker.downcallHandle(lookup.find("munmap").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
            ));
            shm_unlink = linker.downcallHandle(lookup.find("shm_unlink").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
        }
    }

    public MmapSharedMemory(String name, int size, boolean isCreate) throws Throwable {
        super(name, size, isCreate);
        if (CABI.SYSTEM_TYPE != CABI.SystemType.Unix) throw new UnsupportedOperationException("Only Unix is supported");
        int mode = O_RDWR;
        if (isCreate) mode |= O_CREAT | O_EXCL;
        int fd = (int) shm_open.invokeExact(Arena.ofAuto().allocateUtf8String(name), mode, (int) (S_IRUSR | S_IWUSR));
        if (fd == -1) throw new IllegalStateException("shm_open failed.");
        try {
            if (isCreate && (int) ftruncate.invokeExact(fd, size) == -1) throw new IllegalStateException("ftruncate failed.");
            segment = (MemorySegment) mmap.invokeExact(
                    MemorySegment.NULL,
                    size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
            if (segment.address() == -1) throw new IllegalStateException("mmap failed.");
        } catch (Throwable th) {
            close();
            throw th;
        }
        segment = segment.reinterpret(size);
    }

    @Override
    public void close() throws Exception {
        try {
            if (segment != null) munmap.invokeExact(segment, getSize());
            if (isCreate) shm_unlink.invokeExact(Arena.ofAuto().allocateUtf8String(getName()));
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }
}
