package cn.apisium.shm;

import org.jetbrains.annotations.NotNull;

public final class CABI {
    /**
     * System types.
     */
    public enum SystemType { @SuppressWarnings("unused") Unknown, Windows, Unix }

    /**
     * The current system type.
     */
    @NotNull
    public static final SystemType SYSTEM_TYPE;

    static {
        var OS = System.getProperty("os.name");
        if (OS.startsWith("Windows")) SYSTEM_TYPE = SystemType.Windows;
        else SYSTEM_TYPE = SystemType.Unix;
    }
}
