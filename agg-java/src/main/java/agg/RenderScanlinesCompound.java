package agg;

/**
 * Main rendering function for compound rasterization with perfect polygon stitching.
 * Translated from agg_renderer_scanline.h render_scanlines_compound
 */
public class RenderScanlinesCompound {
    
    private static final int COVER_FULL = 255;
    
    /**
     * Renders a compound shape with perfect polygon stitching.
     * 
     * @param ras Compound rasterizer with cell-level style tracking
     * @param slAa AA scanline container for coverage data
     * @param slBin Binary scanline for overlap detection
     * @param ren Base renderer for output
     * @param alloc Span allocator for color buffers
     * @param sh Style handler for style-to-color mapping
     */
    public static void renderScanlinesCompound(
            RasterizerCompoundAa ras,
            ScanlineU8 slAa,
            ScanlineBin slBin,
            RendererBase ren,
            SpanAllocator alloc,
            StyleHandler sh) {
        
        if (ras.rewindScanlines()) {
            int minX = ras.minX();
            int len = ras.maxX() - minX + 2;
            slAa.reset(minX, ras.maxX());
            slBin.reset(minX, ras.maxX());
            
            // Allocate color buffers
            Rgba8[] colorSpan = alloc.allocate(len * 2);
            Rgba8[] mixBuffer = new Rgba8[len];
            
            // Initialize mix buffer
            for (int i = 0; i < len; i++) {
                if (i < colorSpan.length - len) {
                    mixBuffer[i] = colorSpan[len + i];
                } else {
                    mixBuffer[i] = new Rgba8();
                }
            }
            
            int numStyles;
            while ((numStyles = ras.sweepStyles()) > 0) {
                if (numStyles == 1) {
                    // Optimization for a single style
                    if (ras.sweepScanline(slAa, 0)) {
                        int style = ras.style(0);
                        if (sh.isSolid(style)) {
                            // Just solid fill
                            renderScanlineAaSolid(slAa, ren, sh.color(style));
                        } else {
                            // Arbitrary span generator
                            int numSpans = slAa.getNumSpans();
                            for (int i = 0; i < numSpans; i++) {
                                ScanlineU8.Span spanAa = slAa.getSpan(i);
                                int spanLen = spanAa.len;
                                sh.generateSpan(colorSpan, spanAa.x, slAa.getY(), spanLen, style);
                                ren.blendColorHspan(spanAa.x, slAa.getY(), spanLen, 
                                                    colorSpan, spanAa.covers, 0);
                            }
                        }
                    }
                } else {
                    // Multiple styles - use mix buffer
                    if (ras.sweepScanline(slBin, -1)) {
                        // Clear the spans of the mix_buffer
                        int numSpans = slBin.getNumSpans();
                        for (int i = 0; i < numSpans; i++) {
                            ScanlineBin.Span spanBin = slBin.getSpan(i);
                            // Clear mix buffer for this span
                            for (int j = 0; j < spanBin.len; j++) {
                                int idx = spanBin.x - minX + j;
                                if (idx >= 0 && idx < mixBuffer.length) {
                                    mixBuffer[idx].clear();
                                }
                            }
                        }
                        
                        // Process each style
                        for (int i = 0; i < numStyles; i++) {
                            int style = ras.style(i);
                            boolean solid = sh.isSolid(style);
                            
                            if (ras.sweepScanline(slAa, i)) {
                                int spanCount = slAa.getNumSpans();
                                
                                if (solid) {
                                    // Just solid fill
                                    Rgba8 c = sh.color(style);
                                    for (int spanIdx = 0; spanIdx < spanCount; spanIdx++) {
                                        ScanlineU8.Span spanAa = slAa.getSpan(spanIdx);
                                        int spanLen = spanAa.len;
                                        int[] covers = spanAa.covers;
                                        
                                        for (int j = 0; j < spanLen; j++) {
                                            int idx = spanAa.x - minX + j;
                                            if (idx >= 0 && idx < mixBuffer.length) {
                                                int cover = covers[j];
                                                if (cover == COVER_FULL) {
                                                    mixBuffer[idx].set(c);
                                                } else {
                                                    mixBuffer[idx].add(c, cover);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Arbitrary span generator
                                    for (int spanIdx = 0; spanIdx < spanCount; spanIdx++) {
                                        ScanlineU8.Span spanAa = slAa.getSpan(spanIdx);
                                        int spanLen = spanAa.len;
                                        sh.generateSpan(colorSpan, spanAa.x, slAa.getY(), 
                                                       spanLen, style);
                                        int[] covers = spanAa.covers;
                                        
                                        for (int j = 0; j < spanLen; j++) {
                                            int idx = spanAa.x - minX + j;
                                            if (idx >= 0 && idx < mixBuffer.length && j < colorSpan.length) {
                                                int cover = covers[j];
                                                if (cover == COVER_FULL) {
                                                    mixBuffer[idx].set(colorSpan[j]);
                                                } else {
                                                    mixBuffer[idx].add(colorSpan[j], cover);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Emit the blended result as a color hspan
                        numSpans = slBin.getNumSpans();
                        for (int i = 0; i < numSpans; i++) {
                            ScanlineBin.Span spanBin = slBin.getSpan(i);
                            ren.blendColorHspan(spanBin.x, slBin.getY(), spanBin.len,
                                              mixBuffer, null, spanBin.x - minX);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Render a scanline with a solid color.
     * Implements render_scanline_aa_solid from C++ AGG.
     */
    private static void renderScanlineAaSolid(ScanlineU8 sl, RendererBase ren, Rgba8 color) {
        int y = sl.getY();
        int numSpans = sl.getNumSpans();
        
        for (int i = 0; i < numSpans; i++) {
            ScanlineU8.Span span = sl.getSpan(i);
            int x = span.x;
            int len = span.len;
            int[] covers = span.covers;
            
            for (int j = 0; j < len; j++) {
                int alpha = covers[j];
                if (alpha > 0) {
                    ren.blendPixel(x + j, y, color, alpha);
                }
            }
        }
    }
}
