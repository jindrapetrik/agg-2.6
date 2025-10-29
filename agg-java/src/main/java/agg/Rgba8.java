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
// RGBA8 - 8-bit RGBA color
//
//----------------------------------------------------------------------------

package agg;

/**
 * RGBA color with 8-bit components (0-255).
 * Java translation of rgba8 from agg_color_rgba.h (simplified version)
 */
public class Rgba8 {
    
    public static final int BASE_SHIFT = 8;
    public static final int BASE_SCALE = 1 << BASE_SHIFT;  // 256
    public static final int BASE_MASK = BASE_SCALE - 1;     // 255
    
    public int r;
    public int g;
    public int b;
    public int a;
    
    /**
     * Default constructor - creates transparent black.
     */
    public Rgba8() {
        this(0, 0, 0, 0);
    }
    
    /**
     * Constructor with RGB values and optional alpha.
     * 
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @param a alpha component (0-255)
     */
    public Rgba8(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    /**
     * Constructor with RGB values (opaque).
     * 
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     */
    public Rgba8(int r, int g, int b) {
        this(r, g, b, 255);
    }
    
    /**
     * Copy constructor.
     * 
     * @param c color to copy
     */
    public Rgba8(Rgba8 c) {
        this.r = c.r;
        this.g = c.g;
        this.b = c.b;
        this.a = c.a;
    }
    
    /**
     * Constructor from double precision RGBA (0.0-1.0).
     * 
     * @param c double precision color
     */
    public Rgba8(Rgba c) {
        this.r = AggMath.uround(c.r * BASE_MASK);
        this.g = AggMath.uround(c.g * BASE_MASK);
        this.b = AggMath.uround(c.b * BASE_MASK);
        this.a = AggMath.uround(c.a * BASE_MASK);
    }
    
    /**
     * Clear all components to zero.
     * 
     * @return this color for chaining
     */
    public Rgba8 clear() {
        r = g = b = a = 0;
        return this;
    }
    
    /**
     * Make color transparent (alpha = 0).
     * 
     * @return this color for chaining
     */
    public Rgba8 transparent() {
        a = 0;
        return this;
    }
    
    /**
     * Set opacity (alpha channel).
     * 
     * @param a alpha value (0-255)
     * @return this color for chaining
     */
    public Rgba8 opacity(int a) {
        this.a = Math.max(0, Math.min(BASE_MASK, a));
        return this;
    }
    
    /**
     * Get opacity (alpha channel).
     * 
     * @return alpha value (0-255)
     */
    public int opacity() {
        return a;
    }
    
    /**
     * Premultiply RGB components by alpha.
     * 
     * @return this color for chaining
     */
    public Rgba8 premultiply() {
        if (a == BASE_MASK) return this;
        if (a == 0) {
            r = g = b = 0;
            return this;
        }
        r = (r * a + BASE_MASK) >> BASE_SHIFT;
        g = (g * a + BASE_MASK) >> BASE_SHIFT;
        b = (b * a + BASE_MASK) >> BASE_SHIFT;
        return this;
    }
    
    /**
     * Demultiply RGB components (reverse of premultiply).
     * 
     * @return this color for chaining
     */
    public Rgba8 demultiply() {
        if (a == BASE_MASK) return this;
        if (a == 0) {
            r = g = b = 0;
            return this;
        }
        int ra = (BASE_MASK * BASE_SCALE) / a;
        r = (r * ra + BASE_MASK) >> BASE_SHIFT;
        g = (g * ra + BASE_MASK) >> BASE_SHIFT;
        b = (b * ra + BASE_MASK) >> BASE_SHIFT;
        return this;
    }
    
    /**
     * Create a gradient color between this and another color.
     * 
     * @param c target color
     * @param k interpolation factor (0.0 to 1.0)
     * @return new color interpolated between this and c
     */
    public Rgba8 gradient(Rgba8 c, double k) {
        int ik = AggMath.uround(k * BASE_SCALE);
        return new Rgba8(
            r + (((c.r - r) * ik) >> BASE_SHIFT),
            g + (((c.g - g) * ik) >> BASE_SHIFT),
            b + (((c.b - b) * ik) >> BASE_SHIFT),
            a + (((c.a - a) * ik) >> BASE_SHIFT)
        );
    }
    
    /**
     * Add another color to this color.
     * 
     * @param c color to add
     * @return this color for chaining
     */
    public Rgba8 add(Rgba8 c) {
        r = Math.min(r + c.r, BASE_MASK);
        g = Math.min(g + c.g, BASE_MASK);
        b = Math.min(b + c.b, BASE_MASK);
        a = Math.min(a + c.a, BASE_MASK);
        return this;
    }
    
    /**
     * Add another color with coverage.
     * Implements the C++ rgba8::add(c, cover) method.
     * 
     * @param c color to add
     * @param cover coverage value (0-255)
     * @return this color for chaining
     */
    public Rgba8 add(Rgba8 c, int cover) {
        int cr, cg, cb, ca;
        if (cover == BASE_MASK) {
            if (c.a == BASE_MASK) {
                // Full coverage and opaque source - just copy
                set(c);
                return this;
            } else {
                cr = r + c.r;
                cg = g + c.g;
                cb = b + c.b;
                ca = a + c.a;
            }
        } else {
            // Multiply color by coverage
            cr = r + multCover(c.r, cover);
            cg = g + multCover(c.g, cover);
            cb = b + multCover(c.b, cover);
            ca = a + multCover(c.a, cover);
        }
        r = Math.min(cr, BASE_MASK);
        g = Math.min(cg, BASE_MASK);
        b = Math.min(cb, BASE_MASK);
        a = Math.min(ca, BASE_MASK);
        return this;
    }
    
    /**
     * Multiply a component by coverage.
     * Helper method for add(c, cover).
     */
    private static int multCover(int component, int cover) {
        return (component * cover + BASE_MASK) >> BASE_SHIFT;
    }
    
    /**
     * Set this color to match another color.
     * 
     * @param c color to copy
     * @return this color for chaining
     */
    public Rgba8 set(Rgba8 c) {
        r = c.r;
        g = c.g;
        b = c.b;
        a = c.a;
        return this;
    }
    
    /**
     * Convert to double precision RGBA.
     * 
     * @return double precision color
     */
    public Rgba toRgba() {
        return new Rgba(
            r / (double)BASE_MASK,
            g / (double)BASE_MASK,
            b / (double)BASE_MASK,
            a / (double)BASE_MASK
        );
    }
    
    /**
     * Pack color into 32-bit integer (ARGB format).
     * 
     * @return packed color value
     */
    public int toInt() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Create from 32-bit integer (ARGB format).
     * 
     * @param argb packed color value
     * @return new Rgba8 color
     */
    public static Rgba8 fromInt(int argb) {
        return new Rgba8(
            (argb >> 16) & 0xFF,
            (argb >> 8) & 0xFF,
            argb & 0xFF,
            (argb >> 24) & 0xFF
        );
    }
    
    @Override
    public String toString() {
        return String.format("rgba8(%d, %d, %d, %d)", r, g, b, a);
    }
    
    // Common color constants
    public static final Rgba8 BLACK = new Rgba8(0, 0, 0, 255);
    public static final Rgba8 WHITE = new Rgba8(255, 255, 255, 255);
    public static final Rgba8 RED = new Rgba8(255, 0, 0, 255);
    public static final Rgba8 GREEN = new Rgba8(0, 255, 0, 255);
    public static final Rgba8 BLUE = new Rgba8(0, 0, 255, 255);
    public static final Rgba8 TRANSPARENT = new Rgba8(0, 0, 0, 0);
}
