package cn.apisium.shm.impl;

import cn.apisium.shm.AbstractSharedMemory;
import cn.apisium.shm.CABI;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public final class SystemVSharedMemory extends AbstractSharedMemory {
    private static MethodHandle ftok, shmget, shmat, shmdt, shmctl;
    @SuppressWarnings("OctalInteger")
    private static final int IPC_CREAT = 001000, IPC_RMID = 0, IPC_R = 000400, IPC_W = 000200;

    static {
        if (CABI.SYSTEM_TYPE == CABI.SystemType.Unix) {
            var linker = Linker.nativeLinker();
            var lookup = linker.defaultLookup();
            ftok = linker.downcallHandle(lookup.find("ftok").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
            ));
            shmget = linker.downcallHandle(lookup.find("shmget").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ));
            shmat = linker.downcallHandle(lookup.find("shmat").orElseThrow(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
            ));
            shmdt = linker.downcallHandle(lookup.find("shmdt").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS
            ));
            shmctl = linker.downcallHandle(lookup.find("shmctl").orElseThrow(), FunctionDescriptor.ofVoid(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS
            ));
        }
    }
    private final int shmId;

    public SystemVSharedMemory(String name, int size, boolean isCreate) throws Throwable {
        super(name, size, isCreate);
        if (CABI.SYSTEM_TYPE != CABI.SystemType.Unix) throw new UnsupportedOperationException("Only Unix is supported");
        int shmKey = (int) ftok.invokeExact(Arena.ofAuto().allocateUtf8String(name), 0);
        if (shmKey == -1) throw new IllegalStateException("ftok failed.");
        try {
            int shmflg = IPC_R | IPC_W;
            if (isCreate) shmflg |= IPC_CREAT;
            shmId = (int) shmget.invokeExact(shmKey, size, shmflg);
            if (shmId == -1) throw new IllegalStateException("shmget failed.");
            segment = (MemorySegment) shmat.invokeExact(shmId, MemorySegment.NULL, 0);
            if (segment.address() == -1) throw new IllegalStateException("shmat failed.");
        }  catch (Throwable th) {
            close();
            throw th;
        }
        segment = segment.reinterpret(size);
    }

    @Override
    public void close() throws Exception {
        try {
            if (segment != null) shmdt.invokeExact(segment);
            if (shmId != -1 && isCreate) shmctl.invokeExact(shmId, IPC_RMID, MemorySegment.NULL);
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }
}