package cn.apisium.shm.impl;

import cn.apisium.shm.AbstractSharedMemory;
import cn.apisium.shm.CABI;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class WindowsSharedMemory extends AbstractSharedMemory {
    private static MemorySession session;
    private static MethodHandle createFileMapping, openFileMapping, closeHandle, mapViewOfFile, unmapViewOfFile;
    @SuppressWarnings("unused")
    private static final int SECTION_QUERY = 0x0001, SECTION_MAP_WRITE = 0x0002, SECTION_MAP_READ = 0x0004,
            SECTION_MAP_EXECUTE = 0x0008, SECTION_EXTEND_SIZE = 0x0010, SECTION_MAP_EXECUTE_EXPLICIT = 0x0020,
            SECTION_ALL_ACCESS = SECTION_QUERY | SECTION_MAP_WRITE | SECTION_MAP_READ | SECTION_MAP_EXECUTE | SECTION_EXTEND_SIZE | SECTION_MAP_EXECUTE_EXPLICIT;
    @SuppressWarnings("unused")
    private static final int PAGE_NOACCESS = 0x01, PAGE_READONLY = 0x02, PAGE_READWRITE = 0x04, PAGE_WRITECOPY = 0x08,
            PAGE_EXECUTE = 0x10, PAGE_EXECUTE_READ = 0x20, PAGE_EXECUTE_READWRITE = 0x40, PAGE_EXECUTE_WRITECOPY = 0x80,
            PAGE_GUARD = 0x100, PAGE_NOCACHE = 0x200, PAGE_WRITECOMBINE = 0x400;
    @SuppressWarnings("unused")
    private static final int SEC_COMMIT = 0x08000000, SEC_LARGE_PAGES = 0x80000000, FILE_MAP_LARGE_PAGES = 0x20000000;

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
    private final MemoryAddress hMapFile, pBuf;

    public WindowsSharedMemory(String name, int size, boolean isCreate) throws Throwable {
        super(name, size, isCreate);
        if (CABI.SYSTEM_TYPE != CABI.SystemType.Windows) throw new UnsupportedOperationException("Only Windows is supported");
        hMapFile = isCreate ? (MemoryAddress) createFileMapping.invokeExact(
                (Addressable) MemoryAddress.ofLong(-1),
                (Addressable) MemoryAddress.NULL,
                PAGE_READWRITE | SEC_COMMIT,
                0,
                size,
                (Addressable) session.allocateUtf8String(name)
        ) : (MemoryAddress) openFileMapping.invokeExact(SECTION_MAP_WRITE | SECTION_MAP_READ, 0, name);
        if (hMapFile.toRawLongValue() == 0) throw new IllegalStateException("CreateFileMapping failed.");
        try {
            pBuf = (MemoryAddress) mapViewOfFile.invokeExact((Addressable) hMapFile, SECTION_MAP_WRITE | SECTION_MAP_READ, 0, 0, size);
            if (pBuf.toRawLongValue() == 0) throw new IllegalStateException("MapViewOfFile failed.");
        } catch (Throwable th) {
            closeHandle.invokeExact((Addressable) hMapFile);
            throw th;
        }
        segment = MemorySegment.ofAddress(pBuf, size, session);
    }

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