package cn.apisium.shm;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SharedMemoryTest {
    private static SharedMemory shm;
    @BeforeAll
    public static void init() {
        shm = SharedMemory.create("/srgg", 8192);
    }

    @Test
    public void test() {
        var buf = shm.toByteBuffer();
        var array = "Hello, World!".getBytes();
        buf.put(array);
        var array2 = new byte[array.length];
        buf.rewind();
        buf.get(array2);
        Assertions.assertEquals("Hello, World!", new String(array2));
    }

    @Test
    public void testFull() {
        var buf = shm.toByteBuffer();
        var array = new byte[shm.getSize()];
        for (int i = 0; i < shm.getSize(); i++) array[i] = (byte)i;
        buf.put(array);
        var array2 = new byte[shm.getSize()];
        buf.rewind();
        buf.get(array2);
        Assertions.assertArrayEquals(array, array2);
    }

    @AfterAll
    public static void close() throws Exception {
        if (shm != null) shm.close();
    }
}
