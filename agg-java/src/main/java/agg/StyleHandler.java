package agg;

/**
 * Style handler interface for compound rasterization.
 * Maps fill style indices to colors for rendering.
 */
public interface StyleHandler {
    
    /**
     * Generates a span of colors for the given style.
     * 
     * @param span Output color array
     * @param x Starting X coordinate
     * @param y Y coordinate
     * @param len Length of span
     * @param style Style index from compound shape
     */
    void generateSpan(Rgba8[] span, int x, int y, int len, int style);
    
    /**
     * Check if a style is solid (single color).
     * 
     * @param style Style index
     * @return true if style is a solid color
     */
    default boolean isSolid(int style) {
        return true;
    }
    
    /**
     * Get the solid color for a style.
     * 
     * @param style Style index
     * @return Color for this style
     */
    Rgba8 color(int style);
    
    /**
     * Optional preparation hook called before rendering.
     * Can be used to initialize state or precompute values.
     */
    default void prepare() {
        // Default: no preparation needed
    }
}
