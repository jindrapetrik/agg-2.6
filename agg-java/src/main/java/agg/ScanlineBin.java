package agg;

/**
 * Binary scanline container for solid (non-antialiased) rendering.
 * Stores scanline spans with binary coverage (fully covered or not).
 * This is simpler and more memory-efficient than ScanlineU8 for solid fills.
 * 
 * Translated from agg_scanline_bin.h
 */
public class ScanlineBin {
    private int y;
    private int count;
    private int[] spans;  // x, len pairs
    private int spanIndex;
    
    public ScanlineBin() {
        this.spans = new int[256];  // Initial capacity for 128 spans (x, len pairs)
        this.spanIndex = 0;
        this.count = 0;
    }
    
    public void reset(int minX, int maxX) {
        this.spanIndex = 0;
        this.count = 0;
    }
    
    public void addCell(int x, int cover) {
        // Binary scanline - any coverage means fully covered
        if (spanIndex == 0 || spans[spanIndex - 2] + spans[spanIndex - 1] != x) {
            // Start new span
            ensureCapacity(spanIndex + 2);
            spans[spanIndex++] = x;
            spans[spanIndex++] = 1;
            count++;
        } else {
            // Extend current span
            spans[spanIndex - 1]++;
        }
    }
    
    public void addSpan(int x, int len, int cover) {
        // Binary scanline - add span as-is
        ensureCapacity(spanIndex + 2);
        spans[spanIndex++] = x;
        spans[spanIndex++] = len;
        count++;
    }
    
    public void finalize(int y) {
        this.y = y;
    }
    
    public void resetSpans() {
        this.spanIndex = 0;
        this.count = 0;
    }
    
    public int getY() {
        return y;
    }
    
    public int getNumSpans() {
        return count;
    }
    
    public int[] getSpans() {
        return spans;
    }
    
    /**
     * Iterator for traversing spans.
     */
    public static class Iterator {
        private int[] spans;
        private int index;
        private int count;
        
        public Iterator(ScanlineBin scanline) {
            this.spans = scanline.spans;
            this.index = 0;
            this.count = scanline.count;
        }
        
        public int getX() {
            return spans[index];
        }
        
        public int getLen() {
            return spans[index + 1];
        }
        
        public boolean hasNext() {
            return count > 0;
        }
        
        public void next() {
            index += 2;
            count--;
        }
    }
    
    private void ensureCapacity(int required) {
        if (required > spans.length) {
            int newSize = Math.max(required, spans.length * 2);
            int[] newSpans = new int[newSize];
            System.arraycopy(spans, 0, newSpans, 0, spanIndex);
            spans = newSpans;
        }
    }
}
