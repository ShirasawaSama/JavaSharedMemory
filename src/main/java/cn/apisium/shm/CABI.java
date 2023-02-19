package cn.apisium.shm;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class CABI {
    /**
     * System types.
     */
    public enum SystemType { Unknown, Windows, Linux, MacOS }

    /**
     * The current system type.
     */
    public static final SystemType SYSTEM_TYPE;

    static {
        var OS = System.getProperty("os.name");
        var ARCH = System.getProperty("os.arch");
        var ADDRESS_SIZE = ADDRESS.bitSize();
        if ((ARCH.equals("amd64") || ARCH.equals("x86_64")) && ADDRESS_SIZE == 64) {
            SYSTEM_TYPE = OS.startsWith("Windows") ? SystemType.Windows : SystemType.Unknown;
        } else if (ARCH.equals("aarch64")) {
            SYSTEM_TYPE = OS.startsWith("Mac") ? SystemType.MacOS : SystemType.Linux;
        } else SYSTEM_TYPE = SystemType.Unknown;
    }
}
