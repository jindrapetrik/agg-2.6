package agg;

/**
 * Memory allocator for color spans used in rendering.
 * Manages dynamic allocation of span arrays for gradient and pattern fills.
 * 
 * Translated from agg_span_allocator.h
 */
public class SpanAllocator {
    private Rgba8[] span;
    private int maxSpanLen;
    
    public SpanAllocator() {
        this.maxSpanLen = 0;
        this.span = null;
    }
    
    /**
     * Allocate or reallocate span memory for given length.
     */
    public Rgba8[] allocate(int spanLen) {
        if (spanLen > maxSpanLen) {
            // Reallocate with larger size
            span = new Rgba8[spanLen];
            for (int i = 0; i < spanLen; i++) {
                span[i] = new Rgba8();
            }
            maxSpanLen = spanLen;
        }
        return span;
    }
    
    /**
     * Get the current span array.
     */
    public Rgba8[] span() {
        return span;
    }
    
    /**
     * Get maximum allocated span length.
     */
    public int maxSpanLen() {
        return maxSpanLen;
    }
}
