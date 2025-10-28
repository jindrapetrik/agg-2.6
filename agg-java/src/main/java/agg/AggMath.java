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
// Math utility functions
//
//----------------------------------------------------------------------------

package agg;

/**
 * Mathematical utility functions for AGG library.
 * Java translation of agg_math.h
 */
public final class AggMath {
    
    // Private constructor to prevent instantiation
    private AggMath() {}
    
    // Coinciding points maximal distance (Epsilon)
    public static final double VERTEX_DIST_EPSILON = 1e-14;
    
    // Intersection calculation epsilon
    public static final double INTERSECTION_EPSILON = 1.0e-30;
    
    /**
     * Calculate cross product for three points.
     * 
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @param x test point x
     * @param y test point y
     * @return cross product value
     */
    public static double crossProduct(double x1, double y1,
                                     double x2, double y2,
                                     double x, double y) {
        return (x - x2) * (y2 - y1) - (y - y2) * (x2 - x1);
    }
    
    /**
     * Test if a point is inside a triangle.
     * 
     * @param x1 triangle vertex 1 x
     * @param y1 triangle vertex 1 y
     * @param x2 triangle vertex 2 x
     * @param y2 triangle vertex 2 y
     * @param x3 triangle vertex 3 x
     * @param y3 triangle vertex 3 y
     * @param x test point x
     * @param y test point y
     * @return true if point is inside triangle
     */
    public static boolean pointInTriangle(double x1, double y1,
                                         double x2, double y2,
                                         double x3, double y3,
                                         double x, double y) {
        boolean cp1 = crossProduct(x1, y1, x2, y2, x, y) < 0.0;
        boolean cp2 = crossProduct(x2, y2, x3, y3, x, y) < 0.0;
        boolean cp3 = crossProduct(x3, y3, x1, y1, x, y) < 0.0;
        return cp1 == cp2 && cp2 == cp3 && cp3 == cp1;
    }
    
    /**
     * Calculate Euclidean distance between two points.
     * 
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @return distance
     */
    public static double calcDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate squared distance between two points (faster, no sqrt).
     * 
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @return squared distance
     */
    public static double calcSqDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    
    /**
     * Calculate perpendicular distance from point to line.
     * 
     * @param x1 line start x
     * @param y1 line start y
     * @param x2 line end x
     * @param y2 line end y
     * @param x point x
     * @param y point y
     * @return perpendicular distance (signed)
     */
    public static double calcLinePointDistance(double x1, double y1,
                                               double x2, double y2,
                                               double x, double y) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d < VERTEX_DIST_EPSILON) {
            return calcDistance(x1, y1, x, y);
        }
        return ((x - x2) * dy - (y - y2) * dx) / d;
    }
    
    /**
     * Calculate parameter u for point projection on line segment.
     * u = 0 at (x1,y1), u = 1 at (x2,y2)
     * 
     * @param x1 segment start x
     * @param y1 segment start y
     * @param x2 segment end x
     * @param y2 segment end y
     * @param x point x
     * @param y point y
     * @return parameter u
     */
    public static double calcSegmentPointU(double x1, double y1,
                                          double x2, double y2,
                                          double x, double y) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        if (dx == 0 && dy == 0) {
            return 0;
        }
        
        double pdx = x - x1;
        double pdy = y - y1;
        
        return (pdx * dx + pdy * dy) / (dx * dx + dy * dy);
    }
    
    /**
     * Calculate squared distance from point to line segment.
     * 
     * @param x1 segment start x
     * @param y1 segment start y
     * @param x2 segment end x
     * @param y2 segment end y
     * @param x point x
     * @param y point y
     * @param u pre-calculated parameter from calcSegmentPointU
     * @return squared distance to segment
     */
    public static double calcSegmentPointSqDistance(double x1, double y1,
                                                   double x2, double y2,
                                                   double x, double y,
                                                   double u) {
        if (u <= 0) {
            return calcSqDistance(x, y, x1, y1);
        } else if (u >= 1) {
            return calcSqDistance(x, y, x2, y2);
        }
        return calcSqDistance(x, y, x1 + u * (x2 - x1), y1 + u * (y2 - y1));
    }
    
    /**
     * Calculate squared distance from point to line segment.
     * 
     * @param x1 segment start x
     * @param y1 segment start y
     * @param x2 segment end x
     * @param y2 segment end y
     * @param x point x
     * @param y point y
     * @return squared distance to segment
     */
    public static double calcSegmentPointSqDistance(double x1, double y1,
                                                   double x2, double y2,
                                                   double x, double y) {
        return calcSegmentPointSqDistance(x1, y1, x2, y2, x, y,
                calcSegmentPointU(x1, y1, x2, y2, x, y));
    }
    
    /**
     * Calculate intersection point of two lines.
     * 
     * @param ax line 1 point A x
     * @param ay line 1 point A y
     * @param bx line 1 point B x
     * @param by line 1 point B y
     * @param cx line 2 point C x
     * @param cy line 2 point C y
     * @param dx line 2 point D x
     * @param dy line 2 point D y
     * @param result array to store intersection point [x, y] (length >= 2)
     * @return true if lines intersect, false if parallel
     */
    public static boolean calcIntersection(double ax, double ay, double bx, double by,
                                          double cx, double cy, double dx, double dy,
                                          double[] result) {
        double num = (ay - cy) * (dx - cx) - (ax - cx) * (dy - cy);
        double den = (bx - ax) * (dy - cy) - (by - ay) * (dx - cx);
        if (Math.abs(den) < INTERSECTION_EPSILON) {
            return false;
        }
        double r = num / den;
        result[0] = ax + r * (bx - ax);
        result[1] = ay + r * (by - ay);
        return true;
    }
    
    /**
     * Test if two line segments intersect.
     * 
     * @param x1 segment 1 start x
     * @param y1 segment 1 start y
     * @param x2 segment 1 end x
     * @param y2 segment 1 end y
     * @param x3 segment 2 start x
     * @param y3 segment 2 start y
     * @param x4 segment 2 end x
     * @param y4 segment 2 end y
     * @return true if segments intersect
     */
    public static boolean intersectionExists(double x1, double y1, double x2, double y2,
                                            double x3, double y3, double x4, double y4) {
        // Check if bounding boxes intersect
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x4 - x3;
        double dy2 = y4 - y3;
        
        double den = dy1 * dx2 - dx1 * dy2;
        if (Math.abs(den) < INTERSECTION_EPSILON) {
            return false;
        }
        
        double ua = (dx1 * (y3 - y1) - dy1 * (x3 - x1)) / den;
        double ub = (dx2 * (y3 - y1) - dy2 * (x3 - x1)) / den;
        
        return ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1;
    }
    
    /**
     * Fast integer rounding.
     * 
     * @param v value to round
     * @return rounded integer
     */
    public static int iround(double v) {
        return (v < 0.0) ? (int)(v - 0.5) : (int)(v + 0.5);
    }
    
    /**
     * Fast unsigned integer rounding.
     * 
     * @param v value to round
     * @return rounded unsigned integer
     */
    public static int uround(double v) {
        return (int)(v + 0.5);
    }
    
    /**
     * Clamp value to range.
     * 
     * @param x value to clamp
     * @param min minimum value
     * @param max maximum value
     * @return clamped value
     */
    public static double clamp(double x, double min, double max) {
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }
}
