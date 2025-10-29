package agg;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacked scanline container with 8-bit coverage values.
 * Used in rasterization to store spans of pixels with coverage information.
 * Based on agg_scanline_u.h from the C++ AGG library.
 */
public class ScanlineU8 {
    
    public static class Span {
        public int x;
        public int len;
        public int[] covers;
        
        public Span(int x, int len) {
            this.x = x;
            this.len = len;
            this.covers = new int[Math.abs(len)];
        }
    }
    
    private int y;
    private int minX;
    private int maxX;
    private List<Span> spans;
    private int lastX;
    
    public ScanlineU8() {
        spans = new ArrayList<>();
        reset(0, 0);
    }
    
    public void reset(int minX, int maxX) {
        this.minX = minX;
        this.maxX = maxX;
        this.y = 0;
        this.lastX = 0x7FFFFFF0;
        resetSpans();
    }
    
    public void resetSpans() {
        spans.clear();
    }
    
    public void addSpan(int x, int len, int cover) {
        if (len < 0) len = -len;
        Span span = new Span(x, len);
        for (int i = 0; i < len; i++) {
            span.covers[i] = (byte) cover;
        }
        spans.add(span);
    }
    
    public void addCell(int x, int cover) {
        if (x == lastX + 1 && !spans.isEmpty()) {
            Span last = spans.get(spans.size() - 1);
            last.len++;
            byte[] newCovers = new byte[last.len];
            System.arraycopy(last.covers, 0, newCovers, 0, last.covers.length);
            newCovers[last.covers.length] = (byte) cover;
            last.covers = newCovers;
        } else {
            addSpan(x, 1, cover);
        }
        lastX = x;
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
    
    public int numSpans() {
        return spans.size();
    }
    
    public List<Span> spans() {
        return spans;
    }
}
