package agg;

/**
 * 8-bit grayscale color representation.
 * Stores value (v) and alpha (a) components as 8-bit integers (0-255).
 */
public class Gray8 {
    public static final int BASE_SHIFT = 8;
    public static final int BASE_SCALE = 1 << BASE_SHIFT;
    public static final int BASE_MASK = BASE_SCALE - 1;
    
    public static final Gray8 BLACK = new Gray8(0, 255);
    public static final Gray8 WHITE = new Gray8(255, 255);
    public static final Gray8 TRANSPARENT = new Gray8(0, 0);
    
    public int v;  // value (grayscale intensity)
    public int a;  // alpha
    
    public Gray8() {
        this(0, 255);
    }
    
    public Gray8(int v) {
        this(v, 255);
    }
    
    public Gray8(int v, int a) {
        this.v = v & 0xFF;
        this.a = a & 0xFF;
    }
    
    public Gray8(Gray8 other) {
        this.v = other.v;
        this.a = other.a;
    }
    
    /**
     * Converts RGBA color to grayscale using ITU-R BT.709 weights.
     */
    public static Gray8 fromRgba8(Rgba8 c) {
        // Calculate grayscale value as per ITU-R BT.709
        int gray = (55 * c.r + 184 * c.g + 18 * c.b) >> 8;
        return new Gray8(gray, c.a);
    }
    
    /**
     * Converts double-precision RGBA to grayscale.
     */
    public static Gray8 fromRgba(Rgba c) {
        int gray = (int)Math.round((0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b) * BASE_MASK);
        int alpha = (int)Math.round(c.a * 255);
        return new Gray8(gray, alpha);
    }
    
    /**
     * Premultiplies alpha into the value component.
     */
    public Gray8 premultiply() {
        if (a == 255) return this;
        v = (v * a) / 255;
        return this;
    }
    
    /**
     * Removes alpha premultiplication.
     */
    public Gray8 demultiply() {
        if (a == 0 || a == 255) return this;
        v = (v * 255) / a;
        return this;
    }
    
    /**
     * Creates a gradient between two gray colors.
     */
    public Gray8 gradient(Gray8 other, double k) {
        int newV = (int)(v + (other.v - v) * k);
        int newA = (int)(a + (other.a - a) * k);
        return new Gray8(newV, newA);
    }
    
    @Override
    public String toString() {
        return String.format("Gray8(v=%d, a=%d)", v, a);
    }
}
