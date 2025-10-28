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
// Cubic Bezier curve (4 control points)
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Cubic Bezier curve with 4 control points.
 * Simplified Java translation of curve4 from agg_curves.h
 */
public class Curve4 implements VertexSource {
    
    private static final double CURVE_DISTANCE_EPSILON = 1e-30;
    private static final double CURVE_COLLINEARITY_EPSILON = 1e-30;
    private static final double CURVE_ANGLE_TOLERANCE_EPSILON = 0.01;
    
    private double x1, y1, x2, y2, x3, y3, x4, y4;
    private double[] points;
    private int count;
    private int pointIndex;
    private double distanceToleranceSquare;
    private double approximationScale;
    
    /**
     * Default constructor.
     */
    public Curve4() {
        points = new double[64];  // Initial capacity
        count = 0;
        pointIndex = 0;
        distanceToleranceSquare = 0.5 / 2.0;
        distanceToleranceSquare *= distanceToleranceSquare;
        approximationScale = 1.0;
    }
    
    /**
     * Constructor with control points.
     * 
     * @param x1 start point x
     * @param y1 start point y
     * @param x2 first control point x
     * @param y2 first control point y
     * @param x3 second control point x
     * @param y3 second control point y
     * @param x4 end point x
     * @param y4 end point y
     */
    public Curve4(double x1, double y1, double x2, double y2, 
                 double x3, double y3, double x4, double y4) {
        this();
        init(x1, y1, x2, y2, x3, y3, x4, y4);
    }
    
    /**
     * Initialize with control points.
     * 
     * @param x1 start point x
     * @param y1 start point y
     * @param x2 first control point x
     * @param y2 first control point y
     * @param x3 second control point x
     * @param y3 second control point y
     * @param x4 end point x
     * @param y4 end point y
     */
    public void init(double x1, double y1, double x2, double y2,
                    double x3, double y3, double x4, double y4) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        count = 0;
        pointIndex = 0;
    }
    
    /**
     * Set approximation scale.
     * 
     * @param scale scale factor
     */
    public void approximationScale(double scale) {
        approximationScale = scale;
    }
    
    /**
     * Subdivide the curve recursively.
     */
    private void subdivide(double x1, double y1, 
                          double x2, double y2, 
                          double x3, double y3,
                          double x4, double y4, int level) {
        if (level > 16) {
            return;
        }
        
        // Calculate all midpoints
        double x12 = (x1 + x2) / 2;
        double y12 = (y1 + y2) / 2;
        double x23 = (x2 + x3) / 2;
        double y23 = (y2 + y3) / 2;
        double x34 = (x3 + x4) / 2;
        double y34 = (y3 + y4) / 2;
        double x123 = (x12 + x23) / 2;
        double y123 = (y12 + y23) / 2;
        double x234 = (x23 + x34) / 2;
        double y234 = (y23 + y34) / 2;
        double x1234 = (x123 + x234) / 2;
        double y1234 = (y123 + y234) / 2;
        
        double dx = x4 - x1;
        double dy = y4 - y1;
        
        double d2 = Math.abs((x2 - x4) * dy - (y2 - y4) * dx);
        double d3 = Math.abs((x3 - x4) * dy - (y3 - y4) * dx);
        
        if ((d2 + d3) * (d2 + d3) < distanceToleranceSquare * (dx * dx + dy * dy)) {
            addPoint(x1234, y1234);
            return;
        }
        
        subdivide(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
        subdivide(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
    }
    
    private void addPoint(double x, double y) {
        if (count * 2 >= points.length) {
            // Expand array
            double[] newPoints = new double[points.length * 2];
            System.arraycopy(points, 0, newPoints, 0, points.length);
            points = newPoints;
        }
        points[count * 2] = x;
        points[count * 2 + 1] = y;
        count++;
    }
    
    @Override
    public void rewind(int pathId) {
        count = 0;
        pointIndex = 0;
        
        addPoint(x1, y1);
        subdivide(x1, y1, x2, y2, x3, y3, x4, y4, 0);
        addPoint(x4, y4);
        
        pointIndex = 0;
    }
    
    @Override
    public int vertex(double[] xy) {
        if (pointIndex >= count) {
            return PATH_CMD_STOP;
        }
        
        xy[0] = points[pointIndex * 2];
        xy[1] = points[pointIndex * 2 + 1];
        pointIndex++;
        
        return (pointIndex == 1) ? PATH_CMD_MOVE_TO : PATH_CMD_LINE_TO;
    }
}
