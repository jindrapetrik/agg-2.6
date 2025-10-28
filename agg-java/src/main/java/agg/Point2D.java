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
// 2D Point structure
//
//----------------------------------------------------------------------------

package agg;

/**
 * Simple 2D point with double precision coordinates.
 * Utility class for AGG library.
 */
public class Point2D {
    
    public double x;
    public double y;
    
    /**
     * Default constructor - creates point at origin.
     */
    public Point2D() {
        this(0.0, 0.0);
    }
    
    /**
     * Constructor with coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Copy constructor.
     * 
     * @param other point to copy
     */
    public Point2D(Point2D other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * Set coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Set from another point.
     * 
     * @param other point to copy from
     */
    public void set(Point2D other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * Calculate distance to another point.
     * 
     * @param other other point
     * @return Euclidean distance
     */
    public double distanceTo(Point2D other) {
        return AggMath.calcDistance(x, y, other.x, other.y);
    }
    
    /**
     * Calculate squared distance to another point (faster, no sqrt).
     * 
     * @param other other point
     * @return squared distance
     */
    public double distanceSqTo(Point2D other) {
        return AggMath.calcSqDistance(x, y, other.x, other.y);
    }
    
    /**
     * Translate point by offsets.
     * 
     * @param dx x offset
     * @param dy y offset
     * @return this point for chaining
     */
    public Point2D translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }
    
    /**
     * Scale point by factors.
     * 
     * @param sx x scale factor
     * @param sy y scale factor
     * @return this point for chaining
     */
    public Point2D scale(double sx, double sy) {
        this.x *= sx;
        this.y *= sy;
        return this;
    }
    
    /**
     * Check equality with another point (exact match).
     * 
     * @param other other point
     * @return true if coordinates match exactly
     */
    public boolean equals(Point2D other) {
        return this.x == other.x && this.y == other.y;
    }
    
    /**
     * Check approximate equality with another point.
     * 
     * @param other other point
     * @param epsilon tolerance
     * @return true if within tolerance
     */
    public boolean equals(Point2D other, double epsilon) {
        return Math.abs(this.x - other.x) <= epsilon && 
               Math.abs(this.y - other.y) <= epsilon;
    }
    
    @Override
    public String toString() {
        return String.format("Point2D(%.4f, %.4f)", x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point2D other = (Point2D) obj;
        return this.x == other.x && this.y == other.y;
    }
    
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits ^= Double.doubleToLongBits(y) * 31;
        return (int)(bits ^ (bits >>> 32));
    }
}
