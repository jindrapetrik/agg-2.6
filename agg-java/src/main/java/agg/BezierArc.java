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
// Arc generator. Produces at most 4 consecutive cubic bezier curves, i.e., 
// 4, 7, 10, or 13 vertices.
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Bezier arc generator.
 * Produces at most 4 consecutive cubic bezier curves, i.e., 4, 7, 10, or 13 vertices.
 * Java translation of agg_bezier_arc.h and agg_bezier_arc.cpp
 */
public class BezierArc {
    
    // This epsilon is used to prevent us from adding degenerate curves
    private static final double BEZIER_ARC_ANGLE_EPSILON = 0.01;
    
    private int vertex;
    private int numVertices;
    private double[] vertices;
    private int cmd;
    
    /**
     * Default constructor.
     */
    public BezierArc() {
        this.vertex = 26;
        this.numVertices = 0;
        this.vertices = new double[26];
        this.cmd = PATH_CMD_LINE_TO;
    }
    
    /**
     * Constructor with parameters.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param startAngle start angle in radians
     * @param sweepAngle sweep angle in radians
     */
    public BezierArc(double x, double y, double rx, double ry,
                     double startAngle, double sweepAngle) {
        this();
        init(x, y, rx, ry, startAngle, sweepAngle);
    }
    
    /**
     * Initialize the bezier arc.
     * 
     * @param x center x coordinate
     * @param y center y coordinate
     * @param rx x-radius
     * @param ry y-radius
     * @param startAngle start angle in radians
     * @param sweepAngle sweep angle in radians
     */
    public void init(double x, double y, double rx, double ry,
                     double startAngle, double sweepAngle) {
        startAngle = startAngle % (2.0 * PI);
        if (sweepAngle >= 2.0 * PI) sweepAngle = 2.0 * PI;
        if (sweepAngle <= -2.0 * PI) sweepAngle = -2.0 * PI;
        
        if (Math.abs(sweepAngle) < 1e-10) {
            numVertices = 4;
            cmd = PATH_CMD_LINE_TO;
            vertices[0] = x + rx * Math.cos(startAngle);
            vertices[1] = y + ry * Math.sin(startAngle);
            vertices[2] = x + rx * Math.cos(startAngle + sweepAngle);
            vertices[3] = y + ry * Math.sin(startAngle + sweepAngle);
            return;
        }
        
        double totalSweep = 0.0;
        double localSweep = 0.0;
        double prevSweep;
        numVertices = 2;
        cmd = PATH_CMD_CURVE4;
        boolean done = false;
        
        do {
            if (sweepAngle < 0.0) {
                prevSweep = totalSweep;
                localSweep = -PI * 0.5;
                totalSweep -= PI * 0.5;
                if (totalSweep <= sweepAngle + BEZIER_ARC_ANGLE_EPSILON) {
                    localSweep = sweepAngle - prevSweep;
                    done = true;
                }
            } else {
                prevSweep = totalSweep;
                localSweep = PI * 0.5;
                totalSweep += PI * 0.5;
                if (totalSweep >= sweepAngle - BEZIER_ARC_ANGLE_EPSILON) {
                    localSweep = sweepAngle - prevSweep;
                    done = true;
                }
            }
            
            arcToBezier(x, y, rx, ry, startAngle, localSweep,
                       vertices, numVertices - 2);
            
            numVertices += 6;
            startAngle += localSweep;
        } while (!done && numVertices < 26);
    }
    
    /**
     * Convert arc to bezier curve.
     * 
     * @param cx center x
     * @param cy center y
     * @param rx x-radius
     * @param ry y-radius
     * @param startAngle start angle
     * @param sweepAngle sweep angle
     * @param curve output array for curve points
     * @param offset offset in the curve array
     */
    private static void arcToBezier(double cx, double cy, double rx, double ry,
                                    double startAngle, double sweepAngle,
                                    double[] curve, int offset) {
        double x0 = Math.cos(sweepAngle / 2.0);
        double y0 = Math.sin(sweepAngle / 2.0);
        double tx = (1.0 - x0) * 4.0 / 3.0;
        double ty = y0 - tx * x0 / y0;
        
        double[] px = new double[4];
        double[] py = new double[4];
        px[0] = x0;
        py[0] = -y0;
        px[1] = x0 + tx;
        py[1] = -ty;
        px[2] = x0 + tx;
        py[2] = ty;
        px[3] = x0;
        py[3] = y0;
        
        double sn = Math.sin(startAngle + sweepAngle / 2.0);
        double cs = Math.cos(startAngle + sweepAngle / 2.0);
        
        for (int i = 0; i < 4; i++) {
            curve[offset + i * 2] = cx + rx * (px[i] * cs - py[i] * sn);
            curve[offset + i * 2 + 1] = cy + ry * (px[i] * sn + py[i] * cs);
        }
    }
    
    /**
     * Rewind the path to the beginning.
     * 
     * @param pathId path identifier (unused)
     */
    public void rewind(int pathId) {
        vertex = 0;
    }
    
    /**
     * Get the next vertex in the bezier arc path.
     * 
     * @param xy array to receive x and y coordinates (must have length >= 2)
     * @return path command
     */
    public int vertex(double[] xy) {
        if (vertex >= numVertices) {
            return PATH_CMD_STOP;
        }
        xy[0] = vertices[vertex];
        xy[1] = vertices[vertex + 1];
        vertex += 2;
        return (vertex == 2) ? PATH_CMD_MOVE_TO : cmd;
    }
    
    /**
     * Get number of vertices.
     * Note: This returns the doubled number of vertices (pairs).
     * 
     * @return number of vertex coordinates (2 * actual vertices)
     */
    public int numVertices() {
        return numVertices;
    }
    
    /**
     * Get the vertices array.
     * 
     * @return vertices array
     */
    public double[] vertices() {
        return vertices;
    }
}
