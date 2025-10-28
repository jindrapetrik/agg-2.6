package agg;

/**
 * Unpacked scanline container with 8-bit coverage values.
 * Used in rasterization to store spans of pixels with coverage information.
 * Based on agg_scanline_u.h from the C++ AGG library.
 */
public class ScanlineU8 {
    private int y;
    private int minX;
    private int maxX;
    
    public ScanlineU8() {
        reset(0, 0);
    }
    
    public void reset(int minX, int maxX) {
        this.minX = minX;
        this.maxX = maxX;
        this.y = 0;
    }
    
    public void resetSpans() {
        // Reset span storage
    }
    
    public void addSpan(int x, int len, int cover) {
        // Add a span to the scanline
    }
    
    public void addCell(int x, int cover) {
        // Add a single cell
    }
    
    public int y() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int minX() {
        return minX;
    }
    
    public int maxX() {
        return maxX;
    }
}
