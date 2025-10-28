package agg;

/**
 * Vertex command enumeration for AGG paths.
 * These are the basic commands that can be used to construct paths.
 */
public enum PathCommandE {
    STOP(0),
    MOVE_TO(1),
    LINE_TO(2),
    CURVE3(3),
    CURVE4(4),
    CURVE_N(5),
    CATROM(6),
    UBSPLINE(7),
    END_POLY(0x0F),
    MASK(0x0F);
    
    private final int value;
    
    PathCommandE(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static PathCommandE fromValue(int value) {
        value = value & MASK.value;
        for (PathCommandE cmd : values()) {
            if (cmd.value == value) {
                return cmd;
            }
        }
        return STOP;
    }
    
    public static int extractCommand(int cmd) {
        return cmd & MASK.value;
    }
    
    public static boolean isVertex(int cmd) {
        cmd = extractCommand(cmd);
        return cmd >= MOVE_TO.value && cmd < END_POLY.value;
    }
    
    public static boolean isDrawing(int cmd) {
        cmd = extractCommand(cmd);
        return cmd >= LINE_TO.value && cmd < END_POLY.value;
    }
    
    public static boolean isStop(int cmd) {
        return extractCommand(cmd) == STOP.value;
    }
    
    public static boolean isMoveTo(int cmd) {
        return extractCommand(cmd) == MOVE_TO.value;
    }
    
    public static boolean isLineTo(int cmd) {
        return extractCommand(cmd) == LINE_TO.value;
    }
    
    public static boolean isCurve(int cmd) {
        cmd = extractCommand(cmd);
        return cmd == CURVE3.value || cmd == CURVE4.value;
    }
    
    public static boolean isCurve3(int cmd) {
        return extractCommand(cmd) == CURVE3.value;
    }
    
    public static boolean isCurve4(int cmd) {
        return extractCommand(cmd) == CURVE4.value;
    }
    
    public static boolean isEndPoly(int cmd) {
        return extractCommand(cmd) == END_POLY.value;
    }
}
