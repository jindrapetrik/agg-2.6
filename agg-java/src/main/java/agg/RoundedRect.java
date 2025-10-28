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
// Rounded rectangle vertex generator
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Rounded rectangle vertex generator.
 * Java translation of agg_rounded_rect.h and agg_rounded_rect.cpp
 */
public class RoundedRect implements VertexSource {
    
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private double rx1;
    private double ry1;
    private double rx2;
    private double ry2;
    private double rx3;
    private double ry3;
    private double rx4;
    private double ry4;
    private int status;
    private Arc arc;
    
    /**
     * Default constructor.
     */
    public RoundedRect() {
        this.arc = new Arc();
    }
    
    /**
     * Constructor with rectangle and uniform corner radius.
     * 
     * @param x1 left coordinate
     * @param y1 top coordinate
     * @param x2 right coordinate
     * @param y2 bottom coordinate
     * @param r corner radius
     */
    public RoundedRect(double x1, double y1, double x2, double y2, double r) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.rx1 = r;
        this.ry1 = r;
        this.rx2 = r;
        this.ry2 = r;
        this.rx3 = r;
        this.ry3 = r;
        this.rx4 = r;
        this.ry4 = r;
        this.arc = new Arc();
        
        if (x1 > x2) {
            this.x1 = x2;
            this.x2 = x1;
        }
        if (y1 > y2) {
            this.y1 = y2;
            this.y2 = y1;
        }
    }
    
    /**
     * Set rectangle coordinates.
     * 
     * @param x1 left coordinate
     * @param y1 top coordinate
     * @param x2 right coordinate
     * @param y2 bottom coordinate
     */
    public void rect(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        if (x1 > x2) {
            this.x1 = x2;
            this.x2 = x1;
        }
        if (y1 > y2) {
            this.y1 = y2;
            this.y2 = y1;
        }
    }
    
    /**
     * Set uniform corner radius for all corners.
     * 
     * @param r corner radius
     */
    public void radius(double r) {
        this.rx1 = this.ry1 = this.rx2 = this.ry2 = 
        this.rx3 = this.ry3 = this.rx4 = this.ry4 = r;
    }
    
    /**
     * Set corner radius with separate x and y values.
     * 
     * @param rx x radius
     * @param ry y radius
     */
    public void radius(double rx, double ry) {
        this.rx1 = this.rx2 = this.rx3 = this.rx4 = rx;
        this.ry1 = this.ry2 = this.ry3 = this.ry4 = ry;
    }
    
    /**
     * Set corner radius for bottom and top separately.
     * 
     * @param rxBottom bottom x radius
     * @param ryBottom bottom y radius
     * @param rxTop top x radius
     * @param ryTop top y radius
     */
    public void radius(double rxBottom, double ryBottom, 
                      double rxTop, double ryTop) {
        this.rx1 = this.rx2 = rxBottom;
        this.rx3 = this.rx4 = rxTop;
        this.ry1 = this.ry2 = ryBottom;
        this.ry3 = this.ry4 = ryTop;
    }
    
    /**
     * Set individual corner radii.
     * 
     * @param rx1 corner 1 x radius
     * @param ry1 corner 1 y radius
     * @param rx2 corner 2 x radius
     * @param ry2 corner 2 y radius
     * @param rx3 corner 3 x radius
     * @param ry3 corner 3 y radius
     * @param rx4 corner 4 x radius
     * @param ry4 corner 4 y radius
     */
    public void radius(double rx1, double ry1, double rx2, double ry2,
                      double rx3, double ry3, double rx4, double ry4) {
        this.rx1 = rx1;
        this.ry1 = ry1;
        this.rx2 = rx2;
        this.ry2 = ry2;
        this.rx3 = rx3;
        this.ry3 = ry3;
        this.rx4 = rx4;
        this.ry4 = ry4;
    }
    
    /**
     * Normalize corner radii to fit within rectangle.
     */
    public void normalizeRadius() {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        
        double k = 1.0;
        double t;
        
        t = dx / (rx1 + rx2);
        if (t < k) k = t;
        t = dx / (rx3 + rx4);
        if (t < k) k = t;
        t = dy / (ry1 + ry2);
        if (t < k) k = t;
        t = dy / (ry3 + ry4);
        if (t < k) k = t;
        
        if (k < 1.0) {
            rx1 *= k;
            ry1 *= k;
            rx2 *= k;
            ry2 *= k;
            rx3 *= k;
            ry3 *= k;
            rx4 *= k;
            ry4 *= k;
        }
    }
    
    /**
     * Set approximation scale.
     * 
     * @param s scale factor
     */
    public void approximationScale(double s) {
        arc.approximationScale(s);
    }
    
    /**
     * Get approximation scale.
     * 
     * @return scale factor
     */
    public double approximationScale() {
        return arc.approximationScale();
    }
    
    /**
     * Rewind the path to the beginning.
     * 
     * @param pathId path identifier (unused)
     */
    public void rewind(int pathId) {
        status = 0;
    }
    
    /**
     * Get the next vertex in the rounded rectangle path.
     * 
     * @param xy array to receive x and y coordinates (must have length >= 2)
     * @return path command
     */
    public int vertex(double[] xy) {
        int cmd = PATH_CMD_STOP;
        
        switch (status) {
            case 0:
                arc.init(x1 + rx1, y1 + ry1, rx1, ry1, PI, PI + PI * 0.5);
                arc.rewind(0);
                status++;
                // fall through
                
            case 1:
                cmd = arc.vertex(xy);
                if (isStop(cmd)) {
                    status++;
                } else {
                    return cmd;
                }
                // fall through
                
            case 2:
                arc.init(x2 - rx2, y1 + ry2, rx2, ry2, PI + PI * 0.5, 0.0);
                arc.rewind(0);
                status++;
                // fall through
                
            case 3:
                cmd = arc.vertex(xy);
                if (isStop(cmd)) {
                    status++;
                } else {
                    return PATH_CMD_LINE_TO;
                }
                // fall through
                
            case 4:
                arc.init(x2 - rx3, y2 - ry3, rx3, ry3, 0.0, PI * 0.5);
                arc.rewind(0);
                status++;
                // fall through
                
            case 5:
                cmd = arc.vertex(xy);
                if (isStop(cmd)) {
                    status++;
                } else {
                    return PATH_CMD_LINE_TO;
                }
                // fall through
                
            case 6:
                arc.init(x1 + rx4, y2 - ry4, rx4, ry4, PI * 0.5, PI);
                arc.rewind(0);
                status++;
                // fall through
                
            case 7:
                cmd = arc.vertex(xy);
                if (isStop(cmd)) {
                    status++;
                } else {
                    return PATH_CMD_LINE_TO;
                }
                // fall through
                
            case 8:
                cmd = PATH_CMD_END_POLY | PATH_FLAGS_CLOSE | PATH_FLAGS_CCW;
                status++;
                break;
        }
        
        return cmd;
    }
}
