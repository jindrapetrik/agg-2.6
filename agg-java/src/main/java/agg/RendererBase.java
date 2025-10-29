package agg;

/**
 * Base renderer class that provides pixel drawing operations.
 * Works with pixel format buffers to render graphics.
 * Based on agg_renderer_base.h from the C++ AGG library.
 */
public class RendererBase {
    private PixFmtRgba pixf;
    
    public RendererBase(PixFmtRgba pixf) {
        this.pixf = pixf;
    }
    
    public PixFmtRgba pixf() {
        return pixf;
    }
    
    public int width() {
        return pixf.width();
    }
    
    public int height() {
        return pixf.height();
    }
    
    public void clear(Rgba8 color) {
        pixf.rbuf().clear(color);
    }
    
    public void blendSolidHSpan(int x, int y, int len, Rgba8 color, byte[] covers) {
        pixf.blendSolidHSpan(x, y, len, color, covers);
    }
    
    public void blendHLine(int x1, int y, int x2, Rgba8 color, int cover) {
        pixf.blendHLine(x1, y, x2, color, cover);
    }
    
    public void blendPixel(int x, int y, Rgba8 color, int alpha) {
        pixf.blendPixel(x, y, color, alpha);
    }
    
    public void copyPixel(int x, int y, Rgba8 color) {
        pixf.copyPixel(x, y, color);
    }
}
