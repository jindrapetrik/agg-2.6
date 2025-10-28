package agg;

/**
 * Simple 2D rectangle class with integer coordinates.
 * Useful for pixel-perfect operations and bounding boxes.
 */
public class RectI {
    public int x1, y1, x2, y2;
    
    public RectI() {
        this(0, 0, 0, 0);
    }
    
    public RectI(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    public RectI(RectI other) {
        this.x1 = other.x1;
        this.y1 = other.y1;
        this.x2 = other.x2;
        this.y2 = other.y2;
    }
    
    /**
     * Initializes the rectangle to empty state.
     */
    public RectI init(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        return this;
    }
    
    /**
     * Normalizes the rectangle so that x1 <= x2 and y1 <= y2.
     */
    public RectI normalize() {
        if (x1 > x2) { int t = x1; x1 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; }
        return this;
    }
    
    /**
     * Clips this rectangle to another rectangle.
     */
    public boolean clip(RectI r) {
        if (x2 > r.x2) x2 = r.x2;
        if (y2 > r.y2) y2 = r.y2;
        if (x1 < r.x1) x1 = r.x1;
        if (y1 < r.y1) y1 = r.y1;
        return x1 <= x2 && y1 <= y2;
    }
    
    /**
     * Checks if this rectangle is valid (not empty).
     */
    public boolean isValid() {
        return x1 <= x2 && y1 <= y2;
    }
    
    /**
     * Checks if a point is inside the rectangle.
     */
    public boolean hitTest(int x, int y) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
    
    /**
     * Returns the width of the rectangle.
     */
    public int width() {
        return x2 - x1;
    }
    
    /**
     * Returns the height of the rectangle.
     */
    public int height() {
        return y2 - y1;
    }
    
    @Override
    public String toString() {
        return String.format("RectI(%d, %d, %d, %d)", x1, y1, x2, y2);
    }
}
