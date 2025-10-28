//----------------------------------------------------------------------------
// Anti-Grain Geometry - Version 2.6 (Java 8 port)
// Copyright (C) 2002-2005 Maxim Shemanarev (http://www.antigrain.com)
//
// Permission to copy, use, modify, sell and distribute this software 
// is granted provided this copyright notice appears in all copies. 
// This software is provided "as is" without express or implied
// warranty, and with no claim as to its suitability for any purpose.
//
//----------------------------------------------------------------------------
// Contact: mcseem@antigrain.com
//          mcseemagg@yahoo.com
//          http://www.antigrain.com
//----------------------------------------------------------------------------

package agg;

/**
 * Basic types and constants for the AGG library.
 * This is the Java translation of agg_basics.h
 */
public final class AggBasics {
    
    // Private constructor to prevent instantiation
    private AggBasics() {}
    
    // Mathematical constants
    public static final double PI = Math.PI;
    public static final double DEG2RAD = PI / 180.0;
    public static final double RAD2DEG = 180.0 / PI;
    
    // Path commands
    public static final int PATH_CMD_STOP = 0;
    public static final int PATH_CMD_MOVE_TO = 1;
    public static final int PATH_CMD_LINE_TO = 2;
    public static final int PATH_CMD_CURVE3 = 3;
    public static final int PATH_CMD_CURVE4 = 4;
    public static final int PATH_CMD_CURVEN = 5;
    public static final int PATH_CMD_CATROM = 6;
    public static final int PATH_CMD_UBSPLINE = 7;
    public static final int PATH_CMD_END_POLY = 0x0F;
    public static final int PATH_CMD_MASK = 0x0F;
    
    // Path flags
    public static final int PATH_FLAGS_NONE = 0;
    public static final int PATH_FLAGS_CCW = 0x10;
    public static final int PATH_FLAGS_CW = 0x20;
    public static final int PATH_FLAGS_CLOSE = 0x40;
    public static final int PATH_FLAGS_MASK = 0xF0;
    
    // Polygon subpixel constants
    public static final int POLY_SUBPIXEL_SHIFT = 8;
    public static final int POLY_SUBPIXEL_SCALE = 1 << POLY_SUBPIXEL_SHIFT;  // 256
    public static final int POLY_SUBPIXEL_MASK = POLY_SUBPIXEL_SCALE - 1;  // 255
    
    /**
     * Check if a path command is a vertex command.
     */
    public static boolean isVertex(int c) {
        return c >= PATH_CMD_MOVE_TO && c < PATH_CMD_END_POLY;
    }
    
    /**
     * Check if a path command is a drawing command.
     */
    public static boolean isDrawing(int c) {
        return c >= PATH_CMD_LINE_TO && c < PATH_CMD_END_POLY;
    }
    
    /**
     * Check if a path command is stop.
     */
    public static boolean isStop(int c) {
        return c == PATH_CMD_STOP;
    }
    
    /**
     * Check if a path command is move_to.
     */
    public static boolean isMoveTo(int c) {
        return c == PATH_CMD_MOVE_TO;
    }
    
    /**
     * Check if a path command is line_to.
     */
    public static boolean isLineTo(int c) {
        return c == PATH_CMD_LINE_TO;
    }
    
    /**
     * Check if path flags indicate curve.
     */
    public static boolean isCurve(int c) {
        return c == PATH_CMD_CURVE3 || c == PATH_CMD_CURVE4;
    }
    
    /**
     * Check if path flags indicate curve3.
     */
    public static boolean isCurve3(int c) {
        return c == PATH_CMD_CURVE3;
    }
    
    /**
     * Check if path flags indicate curve4.
     */
    public static boolean isCurve4(int c) {
        return c == PATH_CMD_CURVE4;
    }
    
    /**
     * Check if path flags indicate end_poly.
     */
    public static boolean isEndPoly(int c) {
        return (c & PATH_CMD_MASK) == PATH_CMD_END_POLY;
    }
    
    /**
     * Check if path flags indicate close.
     */
    public static boolean isClose(int c) {
        return (c & ~(PATH_FLAGS_CW | PATH_FLAGS_CCW)) == 
               (PATH_CMD_END_POLY | PATH_FLAGS_CLOSE);
    }
    
    /**
     * Check if orientation is counter-clockwise.
     */
    public static boolean isCCW(int c) {
        return (c & PATH_FLAGS_CCW) != 0;
    }
    
    /**
     * Check if orientation is clockwise.
     */
    public static boolean isCW(int c) {
        return (c & PATH_FLAGS_CW) != 0;
    }
    
    /**
     * Check if path flags indicate oriented.
     */
    public static boolean isOriented(int c) {
        return (c & (PATH_FLAGS_CW | PATH_FLAGS_CCW)) != 0;
    }
    
    /**
     * Check if path flags indicate closed.
     */
    public static boolean isClosed(int c) {
        return (c & PATH_FLAGS_CLOSE) != 0;
    }
    
    /**
     * Get command from path flags.
     */
    public static int getCommand(int c) {
        return c & PATH_CMD_MASK;
    }
    
    /**
     * Get flags from path flags.
     */
    public static int getFlags(int c) {
        return c & PATH_FLAGS_MASK;
    }
    
    /**
     * Clear flags from path command.
     */
    public static int clearFlags(int c) {
        return c & PATH_CMD_MASK;
    }
    
    /**
     * Clear orientation flags.
     */
    public static int clearOrientation(int c) {
        return c & ~(PATH_FLAGS_CW | PATH_FLAGS_CCW);
    }
    
    /**
     * Set orientation to CCW.
     */
    public static int setCCW(int c) {
        return c | PATH_FLAGS_CCW;
    }
    
    /**
     * Set orientation to CW.
     */
    public static int setCW(int c) {
        return c | PATH_FLAGS_CW;
    }
    
    /**
     * Get the close flag from path command/flags.
     */
    public static int getCloseFlag(int c) {
        return c & PATH_FLAGS_CLOSE;
    }
    
    /**
     * Integer rounding utility.
     */
    public static int iround(double v) {
        return (int)((v < 0.0) ? v - 0.5 : v + 0.5);
    }
    
    /**
     * Unsigned integer rounding utility.
     */
    public static int uround(double v) {
        return (int)(v + 0.5);
    }
}
