package agg;

/**
 * Pixel format for RGBA color format with pre-multiplied alpha.
 * Provides blending operations for rendering.
 * Based on agg_pixfmt_rgba.h from the C++ AGG library.
 */
public class PixFmtRgba {
    private RenderingBuffer rbuf;
    
    public PixFmtRgba(RenderingBuffer rbuf) {
        this.rbuf = rbuf;
    }
    
    public RenderingBuffer rbuf() {
        return rbuf;
    }
    
    public int width() {
        return rbuf.width();
    }
    
    public int height() {
        return rbuf.height();
    }
    
    /**
     * Blend a horizontal span of pixels with solid color.
     */
    public void blendSolidHSpan(int x, int y, int len, Rgba8 color, byte[] covers) {
        if (y < 0 || y >= rbuf.height()) return;
        
        byte[] buffer = rbuf.buffer();
        int stride = rbuf.stride();
        
        for (int i = 0; i < len; i++) {
            int px = x + i;
            if (px >= 0 && px < rbuf.width()) {
                int offset = y * stride + px * 4;
                int cover = covers[i] & 0xFF;
                
                if (cover == 255) {
                    // Full coverage - direct copy
                    buffer[offset] = (byte) color.b;
                    buffer[offset + 1] = (byte) color.g;
                    buffer[offset + 2] = (byte) color.r;
                    buffer[offset + 3] = (byte) color.a;
                } else if (cover > 0) {
                    // Partial coverage - blend
                    blendPixel(buffer, offset, color, cover);
                }
            }
        }
    }
    
    /**
     * Blend a horizontal line with single coverage value.
     */
    public void blendHLine(int x1, int y, int x2, Rgba8 color, int cover) {
        if (y < 0 || y >= rbuf.height()) return;
        if (x1 > x2) { int t = x1; x1 = x2; x2 = t; }
        
        byte[] buffer = rbuf.buffer();
        int stride = rbuf.stride();
        
        if (cover == 255) {
            // Full coverage
            for (int x = Math.max(0, x1); x <= Math.min(rbuf.width() - 1, x2); x++) {
                int offset = y * stride + x * 4;
                buffer[offset] = (byte) color.b;
                buffer[offset + 1] = (byte) color.g;
                buffer[offset + 2] = (byte) color.r;
                buffer[offset + 3] = (byte) color.a;
            }
        } else if (cover > 0) {
            // Partial coverage - blend
            for (int x = Math.max(0, x1); x <= Math.min(rbuf.width() - 1, x2); x++) {
                int offset = y * stride + x * 4;
                blendPixel(buffer, offset, color, cover);
            }
        }
    }
    
    /**
     * Blend a single pixel with coverage.
     */
    private void blendPixel(byte[] buffer, int offset, Rgba8 color, int cover) {
        // Get destination pixel
        int dstB = buffer[offset] & 0xFF;
        int dstG = buffer[offset + 1] & 0xFF;
        int dstR = buffer[offset + 2] & 0xFF;
        int dstA = buffer[offset + 3] & 0xFF;
        
        // Apply coverage to source alpha
        int alpha = (color.a * cover + 255) >> 8;
        
        if (alpha == 255) {
            // Full opacity
            buffer[offset] = (byte) color.b;
            buffer[offset + 1] = (byte) color.g;
            buffer[offset + 2] = (byte) color.r;
            buffer[offset + 3] = (byte) alpha;
        } else if (alpha > 0) {
            // Blend using pre-multiplied alpha
            int invAlpha = 255 - alpha;
            
            buffer[offset] = (byte) ((color.b * alpha + dstB * invAlpha + 255) >> 8);
            buffer[offset + 1] = (byte) ((color.g * alpha + dstG * invAlpha + 255) >> 8);
            buffer[offset + 2] = (byte) ((color.r * alpha + dstR * invAlpha + 255) >> 8);
            buffer[offset + 3] = (byte) (alpha + ((dstA * invAlpha + 255) >> 8));
        }
    }
}
