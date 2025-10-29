package agg;

/**
 * Simple rendering buffer that holds pixel data in memory.
 * Provides access to pixel rows for rendering operations.
 */
public class RenderingBuffer {
    private byte[] buffer;
    private int width;
    private int height;
    private int stride;
    private int bytesPerPixel;
    
    /**
     * Create a rendering buffer.
     * 
     * @param width image width in pixels
     * @param height image height in pixels
     * @param bytesPerPixel number of bytes per pixel (3 for RGB, 4 for RGBA)
     */
    public RenderingBuffer(int width, int height, int bytesPerPixel) {
        this.width = width;
        this.height = height;
        this.bytesPerPixel = bytesPerPixel;
        this.stride = width * bytesPerPixel;
        this.buffer = new byte[stride * height];
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public int stride() {
        return stride;
    }
    
    public byte[] buffer() {
        return buffer;
    }
    
    /**
     * Get row pointer offset.
     * 
     * @param y row number
     * @return offset into buffer for the row
     */
    public int row(int y) {
        return y * stride;
    }
    
    /**
     * Clear the buffer with a color.
     * 
     * @param color color to fill with
     */
    public void clear(Rgba8 color) {
        int r = color.r;
        int g = color.g;
        int b = color.b;
        int a = bytesPerPixel == 4 ? color.a : 255;
        
        for (int i = 0; i < buffer.length; i += bytesPerPixel) {
            if (bytesPerPixel == 4) {
                buffer[i] = (byte) b;     // BGRA order
                buffer[i + 1] = (byte) g;
                buffer[i + 2] = (byte) r;
                buffer[i + 3] = (byte) a;
            } else {
                buffer[i] = (byte) r;     // RGB order
                buffer[i + 1] = (byte) g;
                buffer[i + 2] = (byte) b;
            }
        }
    }
    
    /**
     * Set a pixel value.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param color color to set
     */
    public void setPixel(int x, int y, Rgba8 color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        
        int offset = y * stride + x * bytesPerPixel;
        int r = color.r;
        int g = color.g;
        int b = color.b;
        int a = bytesPerPixel == 4 ? color.a : 255;
        
        if (bytesPerPixel == 4) {
            buffer[offset] = (byte) b;     // BGRA order
            buffer[offset + 1] = (byte) g;
            buffer[offset + 2] = (byte) r;
            buffer[offset + 3] = (byte) a;
        } else {
            buffer[offset] = (byte) r;     // RGB order
            buffer[offset + 1] = (byte) g;
            buffer[offset + 2] = (byte) b;
        }
    }
    
    /**
     * Blend a pixel value with alpha.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param color color to blend
     * @param alpha alpha value (0-255)
     */
    public void blendPixel(int x, int y, Rgba8 color, int alpha) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        
        int offset = y * stride + x * bytesPerPixel;
        
        // Simple alpha blending
        int srcAlpha = (color.a * alpha) / 255;
        int invAlpha = 255 - srcAlpha;
        
        if (bytesPerPixel == 4) {
            int b = (buffer[offset] & 0xFF);
            int g = (buffer[offset + 1] & 0xFF);
            int r = (buffer[offset + 2] & 0xFF);
            
            buffer[offset] = (byte) ((color.b * srcAlpha + b * invAlpha) / 255);
            buffer[offset + 1] = (byte) ((color.g * srcAlpha + g * invAlpha) / 255);
            buffer[offset + 2] = (byte) ((color.r * srcAlpha + r * invAlpha) / 255);
            buffer[offset + 3] = (byte) Math.max(buffer[offset + 3] & 0xFF, srcAlpha);
        } else {
            int r = (buffer[offset] & 0xFF);
            int g = (buffer[offset + 1] & 0xFF);
            int b = (buffer[offset + 2] & 0xFF);
            
            buffer[offset] = (byte) ((color.r * srcAlpha + r * invAlpha) / 255);
            buffer[offset + 1] = (byte) ((color.g * srcAlpha + g * invAlpha) / 255);
            buffer[offset + 2] = (byte) ((color.b * srcAlpha + b * invAlpha) / 255);
        }
    }
}
