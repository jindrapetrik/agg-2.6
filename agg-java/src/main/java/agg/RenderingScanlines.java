package agg;

/**
 * Utility class for rendering scanlines.
 * Provides methods to render rasterized paths using scanlines.
 * Based on agg_renderer_scanline.h from the C++ AGG library.
 */
public class RenderingScanlines {
    
    /**
     * Render scanlines from rasterizer using a scanline renderer.
     */
    public static void renderScanlines(RasterizerScanlineAa ras, 
                                      ScanlineU8 sl, 
                                      RendererScanlineAaSolid ren) {
        if (ras.rewindScanlines()) {
            sl.reset(ras.minX(), ras.maxX());
            while (ras.sweepScanline(sl)) {
                ren.render(sl);
            }
        }
    }
    
    /**
     * Render scanlines with solid color (convenience method).
     */
    public static void renderScanlines(RasterizerScanlineAa ras,
                                      ScanlineU8 sl,
                                      RendererBase base,
                                      Rgba8 color) {
        RendererScanlineAaSolid ren = new RendererScanlineAaSolid(base);
        ren.color(color);
        renderScanlines(ras, sl, ren);
    }
}
