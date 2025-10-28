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
// class Ellipse
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Ellipse generator.
 * Java translation of agg_ellipse.h
 */
public class Ellipse {
    
    private double x;
    private double y;
    private double rx;
    private double ry;
    private double scale;
    private int num;
    private int step;
    private boolean cw;
    
    /**
     * Default constructor.
     */
    public Ellipse() {
        this.x = 0.0;
        this.y = 0.0;
        this.rx = 1.0;
        this.ry = 1.0;
        this.scale = 1.0;
        this.num = 4;
        this.step = 0;
        this.cw = false;
    }
    
    /**
     * Constructor with parameters.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param numSteps number of steps (0 for automatic)
     * @param cw clockwise flag
     */
    public Ellipse(double x, double y, double rx, double ry, 
                   int numSteps, boolean cw) {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        this.scale = 1.0;
        this.num = numSteps;
        this.step = 0;
        this.cw = cw;
        if (this.num == 0) {
            calcNumSteps();
        }
    }
    
    /**
     * Constructor with automatic step calculation.
     */
    public Ellipse(double x, double y, double rx, double ry) {
        this(x, y, rx, ry, 0, false);
    }
    
    /**
     * Initialize the ellipse with parameters.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param numSteps number of steps (0 for automatic)
     * @param cw clockwise flag
     */
    public void init(double x, double y, double rx, double ry,
                     int numSteps, boolean cw) {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        this.num = numSteps;
        this.step = 0;
        this.cw = cw;
        if (this.num == 0) {
            calcNumSteps();
        }
    }
    
    /**
     * Initialize with automatic step calculation.
     */
    public void init(double x, double y, double rx, double ry) {
        init(x, y, rx, ry, 0, false);
    }
    
    /**
     * Set approximation scale.
     * 
     * @param scale scale factor
     */
    public void approximationScale(double scale) {
        this.scale = scale;
        calcNumSteps();
    }
    
    /**
     * Rewind the path to the beginning.
     * 
     * @param pathId path identifier (unused)
     */
    public void rewind(int pathId) {
        this.step = 0;
    }
    
    /**
     * Get the next vertex in the ellipse path.
     * 
     * @param xy array to receive x and y coordinates (must have length >= 2)
     * @return path command
     */
    public int vertex(double[] xy) {
        if (step == num) {
            step++;
            return PATH_CMD_END_POLY | PATH_FLAGS_CLOSE | PATH_FLAGS_CCW;
        }
        if (step > num) {
            return PATH_CMD_STOP;
        }
        
        double angle = (double) step / (double) num * 2.0 * PI;
        if (cw) {
            angle = 2.0 * PI - angle;
        }
        
        xy[0] = x + Math.cos(angle) * rx;
        xy[1] = y + Math.sin(angle) * ry;
        step++;
        
        return (step == 1) ? PATH_CMD_MOVE_TO : PATH_CMD_LINE_TO;
    }
    
    /**
     * Calculate the number of steps for approximation.
     */
    private void calcNumSteps() {
        double ra = (Math.abs(rx) + Math.abs(ry)) / 2.0;
        double da = Math.acos(ra / (ra + 0.125 / scale)) * 2.0;
        num = (int) Math.round(2.0 * PI / da);
    }
}
