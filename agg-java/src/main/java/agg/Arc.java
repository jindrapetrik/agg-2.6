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
//
// Arc vertex generator
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Arc vertex generator.
 * Java translation of agg_arc.h and agg_arc.cpp
 */
public class Arc {
    
    private double x;
    private double y;
    private double rx;
    private double ry;
    private double angle;
    private double start;
    private double end;
    private double scale;
    private double da;
    private boolean ccw;
    private boolean initialized;
    private int pathCmd;
    
    /**
     * Default constructor.
     */
    public Arc() {
        this.scale = 1.0;
        this.initialized = false;
    }
    
    /**
     * Constructor with parameters.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param a1 start angle in radians
     * @param a2 end angle in radians
     * @param ccw counter-clockwise flag
     */
    public Arc(double x, double y, double rx, double ry, 
               double a1, double a2, boolean ccw) {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        this.scale = 1.0;
        normalize(a1, a2, ccw);
    }
    
    /**
     * Constructor with default ccw = true.
     */
    public Arc(double x, double y, double rx, double ry, 
               double a1, double a2) {
        this(x, y, rx, ry, a1, a2, true);
    }
    
    /**
     * Initialize the arc with parameters.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param a1 start angle in radians
     * @param a2 end angle in radians
     * @param ccw counter-clockwise flag
     */
    public void init(double x, double y, double rx, double ry,
                     double a1, double a2, boolean ccw) {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        normalize(a1, a2, ccw);
    }
    
    /**
     * Initialize with default ccw = true.
     */
    public void init(double x, double y, double rx, double ry,
                     double a1, double a2) {
        init(x, y, rx, ry, a1, a2, true);
    }
    
    /**
     * Set approximation scale.
     * 
     * @param s scale factor
     */
    public void approximationScale(double s) {
        this.scale = s;
        if (initialized) {
            normalize(start, end, ccw);
        }
    }
    
    /**
     * Get approximation scale.
     * 
     * @return current scale factor
     */
    public double approximationScale() {
        return scale;
    }
    
    /**
     * Rewind the path to the beginning.
     * 
     * @param pathId path identifier (unused)
     */
    public void rewind(int pathId) {
        pathCmd = PATH_CMD_MOVE_TO;
        angle = start;
    }
    
    /**
     * Get the next vertex in the arc path.
     * 
     * @param xy array to receive x and y coordinates (must have length >= 2)
     * @return path command
     */
    public int vertex(double[] xy) {
        if (isStop(pathCmd)) {
            return PATH_CMD_STOP;
        }
        
        if ((angle < end - da / 4) != ccw) {
            xy[0] = x + Math.cos(end) * rx;
            xy[1] = y + Math.sin(end) * ry;
            pathCmd = PATH_CMD_STOP;
            return PATH_CMD_LINE_TO;
        }
        
        xy[0] = x + Math.cos(angle) * rx;
        xy[1] = y + Math.sin(angle) * ry;
        
        angle += da;
        
        int pf = pathCmd;
        pathCmd = PATH_CMD_LINE_TO;
        return pf;
    }
    
    /**
     * Normalize the arc angles.
     * 
     * @param a1 start angle
     * @param a2 end angle
     * @param ccw counter-clockwise flag
     */
    private void normalize(double a1, double a2, boolean ccw) {
        double ra = (Math.abs(rx) + Math.abs(ry)) / 2.0;
        da = Math.acos(ra / (ra + 0.125 / scale)) * 2.0;
        
        if (ccw) {
            while (a2 < a1) {
                a2 += PI * 2.0;
            }
        } else {
            while (a1 < a2) {
                a1 += PI * 2.0;
            }
            da = -da;
        }
        
        this.ccw = ccw;
        this.start = a1;
        this.end = a2;
        this.initialized = true;
    }
}
