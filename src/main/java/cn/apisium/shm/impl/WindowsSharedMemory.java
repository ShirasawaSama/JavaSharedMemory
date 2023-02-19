package cn.apisium.shm.impl;

import cn.apisium.shm.CABI;
import cn.apisium.shm.SharedMemory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

public class WindowsSharedMemory implements SharedMemory {
    private static MemorySession session;
    private static MethodHandle createFileMapping, openFileMapping, closeHandle, mapViewOfFile, unmapViewOfFile;

    static {
        if (CABI.SYSTEM_TYPE == CABI.SystemType.Windows) {
            var linker = Linker.nativeLinker();
            session = MemorySession.openImplicit();
            var kernel = SymbolLookup.libraryLookup("kernel32.dll", session);
            createFileMapping = linker.downcallHandle(kernel.lookup("CreateFileMappingA").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS
            ));
            openFileMapping = linker.downcallHandle(kernel.lookup("OpenFileMappingA").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS
            ));
            closeHandle = linker.downcallHandle(kernel.lookup("CloseHandle").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
            mapViewOfFile = linker.downcallHandle(kernel.lookup("MapViewOfFile").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ));
            unmapViewOfFile = linker.downcallHandle(kernel.lookup("UnmapViewOfFile").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
        }
    }

    private final int size;
    private final String name;
    private final MemoryAddress hMapFile, pBuf;
    private final MemorySegment segment;

    @SuppressWarnings("unused")
    public WindowsSharedMemory(String name, int size) throws Throwable { this(name, size, true); }
    public WindowsSharedMemory(String name, int size, boolean isCreate) throws Throwable {
        if (CABI.SYSTEM_TYPE != CABI.SystemType.Windows) throw new UnsupportedOperationException("Only Windows is supported");
        this.size = size;
        this.name = name;
        hMapFile = isCreate ? (MemoryAddress) createFileMapping.invokeExact(
                (Addressable) MemoryAddress.ofLong(-1),
                (Addressable) MemoryAddress.NULL,
                0x04,
                0,
                size,
                (Addressable) session.allocateUtf8String(name)
        ) : (MemoryAddress) openFileMapping.invokeExact(0x02, 0, name);
        if (hMapFile.toRawLongValue() == 0) throw new IllegalStateException("CreateFileMapping failed");
        try {
            pBuf = (MemoryAddress) mapViewOfFile.invokeExact((Addressable) hMapFile, 0x02, 0, 0, 1024);
            if (pBuf.toRawLongValue() == 0) throw new IllegalStateException("CreateFileMapping failed");
        } catch (Throwable th) {
            closeHandle.invokeExact((Addressable) hMapFile);
            throw th;
        }
        segment = MemorySegment.ofAddress(pBuf, size, session);
    }

    @Override
    public ByteBuffer toByteBuffer() { return segment.asByteBuffer(); }

    @Override
    public int size() { return size; }

    @Override
    public String getName() { return name; }

    @Override
    public void close() throws Exception {
        try {
            unmapViewOfFile.invokeExact((Addressable) pBuf);
            closeHandle.invokeExact((Addressable) hMapFile);
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }
}