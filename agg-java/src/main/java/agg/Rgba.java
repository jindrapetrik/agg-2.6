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
// RGBA color type
//
//----------------------------------------------------------------------------

package agg;

/**
 * RGBA color representation with double precision components.
 * Java translation of agg_color_rgba.h (simplified version)
 */
public class Rgba {
    
    public double r;
    public double g;
    public double b;
    public double a;
    
    /**
     * Default constructor - creates black transparent color.
     */
    public Rgba() {
        this.r = 0.0;
        this.g = 0.0;
        this.b = 0.0;
        this.a = 0.0;
    }
    
    /**
     * Constructor with RGB values and optional alpha.
     * 
     * @param r red component (0.0 to 1.0)
     * @param g green component (0.0 to 1.0)
     * @param b blue component (0.0 to 1.0)
     * @param a alpha component (0.0 to 1.0), default 1.0
     */
    public Rgba(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    /**
     * Constructor with RGB values (opaque).
     */
    public Rgba(double r, double g, double b) {
        this(r, g, b, 1.0);
    }
    
    /**
     * Copy constructor with new alpha.
     * 
     * @param c color to copy
     * @param a new alpha value
     */
    public Rgba(Rgba c, double a) {
        this.r = c.r;
        this.g = c.g;
        this.b = c.b;
        this.a = a;
    }
    
    /**
     * Clear all components to zero.
     * 
     * @return this color for chaining
     */
    public Rgba clear() {
        r = g = b = a = 0.0;
        return this;
    }
    
    /**
     * Make color transparent (alpha = 0).
     * 
     * @return this color for chaining
     */
    public Rgba transparent() {
        a = 0.0;
        return this;
    }
    
    /**
     * Set opacity (alpha channel).
     * 
     * @param a alpha value (clamped to 0.0-1.0)
     * @return this color for chaining
     */
    public Rgba opacity(double a) {
        if (a < 0.0) {
            this.a = 0.0;
        } else if (a > 1.0) {
            this.a = 1.0;
        } else {
            this.a = a;
        }
        return this;
    }
    
    /**
     * Get opacity (alpha channel).
     * 
     * @return alpha value
     */
    public double opacity() {
        return a;
    }
    
    /**
     * Premultiply RGB components by alpha.
     * 
     * @return this color for chaining
     */
    public Rgba premultiply() {
        r *= a;
        g *= a;
        b *= a;
        return this;
    }
    
    /**
     * Premultiply with a different alpha value.
     * 
     * @param a alpha value to premultiply with
     * @return this color for chaining
     */
    public Rgba premultiply(double a) {
        if (this.a <= 0.0 || a <= 0.0) {
            r = g = b = this.a = 0.0;
        } else {
            double factor = a / this.a;
            r *= factor;
            g *= factor;
            b *= factor;
            this.a = a;
        }
        return this;
    }
    
    /**
     * Demultiply RGB components (reverse of premultiply).
     * 
     * @return this color for chaining
     */
    public Rgba demultiply() {
        if (a == 0.0) {
            r = g = b = 0.0;
        } else {
            double factor = 1.0 / a;
            r *= factor;
            g *= factor;
            b *= factor;
        }
        return this;
    }
    
    /**
     * Create a gradient color between this and another color.
     * 
     * @param c target color
     * @param k interpolation factor (0.0 to 1.0)
     * @return new color interpolated between this and c
     */
    public Rgba gradient(Rgba c, double k) {
        return new Rgba(
            r + (c.r - r) * k,
            g + (c.g - g) * k,
            b + (c.b - b) * k,
            a + (c.a - a) * k
        );
    }
    
    /**
     * Add another color to this color.
     * 
     * @param c color to add
     * @return this color for chaining
     */
    public Rgba add(Rgba c) {
        r += c.r;
        g += c.g;
        b += c.b;
        a += c.a;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("rgba(%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
    }
    
    // Common color constants
    public static final Rgba BLACK = new Rgba(0, 0, 0, 1);
    public static final Rgba WHITE = new Rgba(1, 1, 1, 1);
    public static final Rgba RED = new Rgba(1, 0, 0, 1);
    public static final Rgba GREEN = new Rgba(0, 1, 0, 1);
    public static final Rgba BLUE = new Rgba(0, 0, 1, 1);
    public static final Rgba TRANSPARENT = new Rgba(0, 0, 0, 0);
}
