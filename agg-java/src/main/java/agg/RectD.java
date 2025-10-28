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
// Rectangle class
//
//----------------------------------------------------------------------------

package agg;

/**
 * Simple rectangle class with double precision coordinates.
 * Java translation of rect_base template from AGG.
 */
public class RectD {
    
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    
    /**
     * Default constructor - creates empty rectangle at origin.
     */
    public RectD() {
        this(0.0, 0.0, 0.0, 0.0);
    }
    
    /**
     * Constructor with coordinates.
     * 
     * @param x1 left coordinate
     * @param y1 top coordinate
     * @param x2 right coordinate
     * @param y2 bottom coordinate
     */
    public RectD(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    /**
     * Initialize rectangle with coordinates.
     * 
     * @param x1 left coordinate
     * @param y1 top coordinate
     * @param x2 right coordinate
     * @param y2 bottom coordinate
     * @return this rectangle for chaining
     */
    public RectD init(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        return this;
    }
    
    /**
     * Normalize the rectangle so that x1 <= x2 and y1 <= y2.
     * 
     * @return this rectangle for chaining
     */
    public RectD normalize() {
        double t;
        if (x1 > x2) {
            t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            t = y1;
            y1 = y2;
            y2 = t;
        }
        return this;
    }
    
    /**
     * Clip rectangle to another rectangle.
     * 
     * @param r rectangle to clip to
     * @return true if rectangles intersect, false otherwise
     */
    public boolean clip(RectD r) {
        if (x2 > r.x2) x2 = r.x2;
        if (y2 > r.y2) y2 = r.y2;
        if (x1 < r.x1) x1 = r.x1;
        if (y1 < r.y1) y1 = r.y1;
        return x1 <= x2 && y1 <= y2;
    }
    
    /**
     * Check if rectangle is valid (x1 <= x2 and y1 <= y2).
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return x1 <= x2 && y1 <= y2;
    }
    
    /**
     * Check if point is inside rectangle.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if point is inside
     */
    public boolean hitTest(double x, double y) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
    
    /**
     * Get width of rectangle.
     * 
     * @return width
     */
    public double width() {
        return x2 - x1;
    }
    
    /**
     * Get height of rectangle.
     * 
     * @return height
     */
    public double height() {
        return y2 - y1;
    }
    
    @Override
    public String toString() {
        return String.format("RectD(%.2f, %.2f, %.2f, %.2f)", x1, y1, x2, y2);
    }
}
