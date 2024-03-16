package cn.apisium.shm.impl;

import cn.apisium.shm.AbstractSharedMemory;
import cn.apisium.shm.CABI;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class WindowsSharedMemory extends AbstractSharedMemory {
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
            var kernel = SymbolLookup.libraryLookup("kernel32.dll", Arena.global());
            createFileMapping = linker.downcallHandle(kernel.find("CreateFileMappingA").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS
            ));
            openFileMapping = linker.downcallHandle(kernel.find("OpenFileMappingA").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS
            ));
            closeHandle = linker.downcallHandle(kernel.find("CloseHandle").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
            mapViewOfFile = linker.downcallHandle(kernel.find("MapViewOfFile").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ));
            unmapViewOfFile = linker.downcallHandle(kernel.find("UnmapViewOfFile").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
        }
    }
    private final MemorySegment hMapFile;

    public WindowsSharedMemory(String name, int size, boolean isCreate) throws Throwable {
        super(name, size, isCreate);
        if (CABI.SYSTEM_TYPE != CABI.SystemType.Windows) throw new UnsupportedOperationException("Only Windows is supported");
        hMapFile = isCreate ? (MemorySegment) createFileMapping.invokeExact(
                (MemorySegment) MemorySegment.ofAddress(-1),
                MemorySegment.NULL,
                PAGE_READWRITE | SEC_COMMIT,
                0,
                size,
                (MemorySegment) Arena.ofAuto().allocateUtf8String(name)
        ) : (MemorySegment) openFileMapping.invokeExact(SECTION_MAP_WRITE | SECTION_MAP_READ, 0, (MemorySegment) Arena.ofAuto().allocateUtf8String(name));
        if (hMapFile.address() == 0) throw new IllegalStateException("CreateFileMapping failed.");
        try {
            segment = (MemorySegment) mapViewOfFile.invokeExact(hMapFile, SECTION_MAP_WRITE | SECTION_MAP_READ, 0, 0, size);
            if (segment.address() == 0) throw new IllegalStateException("MapViewOfFile failed.");
        } catch (Throwable th) {
            closeHandle.invokeExact(hMapFile);
            throw th;
        }
        segment = segment.reinterpret(size);
    }

    @Override
    public void close() throws Exception {
        try {
            if (segment != null) unmapViewOfFile.invokeExact(segment);
            if (hMapFile != null) closeHandle.invokeExact(hMapFile);
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }
}
