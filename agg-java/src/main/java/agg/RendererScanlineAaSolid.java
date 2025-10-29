package agg;

/**
 * Scanline renderer for solid color fills with anti-aliasing.
 * Based on agg_renderer_scanline.h from the C++ AGG library.
 */
public class RendererScanlineAaSolid {
    private RendererBase base;
    private Rgba8 color;
    
    public RendererScanlineAaSolid(RendererBase base) {
        this.base = base;
        this.color = new Rgba8(0, 0, 0, 255);
    }
    
    public void color(Rgba8 color) {
        this.color = color;
    }
    
    public Rgba8 color() {
        return color;
    }
    
    /**
     * Render a single scanline.
     */
    public void render(ScanlineU8 sl) {
        int y = sl.y();
        int numSpans = sl.numSpans();
        
        for (ScanlineU8.Span span : sl.spans()) {
            int x = span.x;
            int len = span.len;
            
            if (len > 0) {
                // Span with individual coverage values
                // Convert int[] to byte[] for blendSolidHSpan
                byte[] byteCovers = new byte[span.covers.length];
                for (int i = 0; i < span.covers.length; i++) {
                    byteCovers[i] = (byte) span.covers[i];
                }
                base.blendSolidHSpan(x, y, len, color, byteCovers);
            } else {
                // Solid span with single coverage
                base.blendHLine(x, y, x - len - 1, color, span.covers[0] & 0xFF);
            }
        }
    }
}
