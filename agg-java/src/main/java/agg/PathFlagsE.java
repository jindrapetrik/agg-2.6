package agg;

/**
 * Path flags enumeration for AGG paths.
 * These flags can be combined with path commands to specify additional properties.
 */
public enum PathFlagsE {
    NONE(0),
    CCW(0x10),
    CW(0x20),
    CLOSE(0x40);
    
    private final int value;
    
    PathFlagsE(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static boolean hasFlag(int cmd, PathFlagsE flag) {
        return (cmd & flag.value) != 0;
    }
    
    public static int setFlag(int cmd, PathFlagsE flag) {
        return cmd | flag.value;
    }
    
    public static int clearFlag(int cmd, PathFlagsE flag) {
        return cmd & ~flag.value;
    }
}
