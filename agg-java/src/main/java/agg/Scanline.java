package agg;

/**
 * Base interface for scanline containers.
 * Scanlines store coverage information for a horizontal line of pixels.
 */
public interface Scanline {
    /**
     * Reset the scanline for reuse
     */
    void resetSpans();
    
    /**
     * Add a single cell with coverage
     */
    void addCell(int x, int cover);
    
    /**
     * Add a span of pixels with same coverage
     */
    void addSpan(int x, int len, int cover);
    
    /**
     * Finalize the scanline at given Y coordinate
     */
    void finalize(int y);
    
    /**
     * Get number of spans in this scanline
     */
    int numSpans();
    
    /**
     * Get Y coordinate of this scanline
     */
    int y();
}
