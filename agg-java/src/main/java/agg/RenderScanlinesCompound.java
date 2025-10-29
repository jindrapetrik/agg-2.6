package agg;

/**
 * Main rendering function for compound rasterization with perfect polygon stitching.
 * 
 * This is the heart of the compound rasterizer system. It handles both simple single-style
 * scanlines (fast path) and complex multi-style scanlines (quality path with blending).
 * 
 * Algorithm:
 * 1. Iterate through scanlines from minY to maxY
 * 2. For each scanline, call ras.sweepStyles() to get count of active styles
 * 3. Single style: Direct render (fast)
 * 4. Multiple styles: Blend colors pixel-by-pixel in mix buffer, then composite
 * 
 * The blending ensures perfect stitching at polygon boundaries with no gaps.
 */
public class RenderScanlinesCompound {
    
    /**
     * Renders a compound shape with perfect polygon stitching.
     * 
     * @param ras Compound rasterizer with cell-level style tracking
     * @param sl Scanline container for coverage data
     * @param slBin Binary scanline for overlap detection
     * @param renBase Base renderer for output
     * @param alloc Span allocator for color buffers
     * @param sh Style handler for style-to-color mapping
     */
    public static void renderScanlinesCompound(
            RasterizerCompoundAa ras,
            ScanlineU8 sl,
            ScanlineBin slBin,
            RendererBase renBase,
            SpanAllocator alloc,
            StyleHandler sh) {
        
        if (ras.rewindScanlines()) {
            // Prepare style handler
            sh.prepare();
            
            // Min/max coordinates for scanline allocation
            int minX = ras.minX();
            int maxX = ras.maxX();
            
            // Process each scanline
            do {
                // Get count of active styles on this scanline
                int numStyles = ras.sweepStyles();
                
                if (numStyles > 0) {
                    int y = ras.scanlineStart();
                    
                    if (numStyles == 1) {
                        // ============ FAST PATH: Single style ============
                        // Only one fill style on this scanline, render directly
                        
                        if (ras.sweepScanline(sl, 0)) {
                            int styleId = ras.style(0);
                            
                            // Generate color span for this style
                            Rgba8[] colors = alloc.allocate(maxX - minX + 2);
                            sh.generateSpan(colors, minX, y, maxX - minX + 2, styleId);
                            
                            // Render the scanline
                            renderScanlineSolid(sl, renBase, colors, minX);
                        }
                    } else {
                        // ============ QUALITY PATH: Multiple styles ============
                        // Multiple fills overlap on this scanline, need to blend
                        
                        // Allocate mix buffer for blended colors
                        int scanLen = maxX - minX + 2;
                        Rgba8[] mixColors = alloc.allocate(scanLen);
                        int[] coverageBuffer = new int[scanLen];
                        
                        // Clear mix buffer - all entries already have Rgba8 objects from allocator
                        for (int i = 0; i < scanLen; i++) {
                            mixColors[i].r = 0;
                            mixColors[i].g = 0;
                            mixColors[i].b = 0;
                            mixColors[i].a = 0;
                            coverageBuffer[i] = 0;
                        }
                        
                        // Process each style
                        for (int i = 0; i < numStyles; i++) {
                            if (ras.sweepScanline(sl, i)) {
                                int styleId = ras.style(i);
                                
                                // Generate color for this style
                                Rgba8[] styleColors = new Rgba8[scanLen];
                                sh.generateSpan(styleColors, minX, y, scanLen, styleId);
                                
                                // Blend this style's coverage into mix buffer
                                blendStyleIntoMix(sl, styleColors, mixColors, coverageBuffer, minX);
                            }
                        }
                        
                        // Composite the blended scanline to renderer
                        compositeMixBuffer(mixColors, coverageBuffer, renBase, y, minX, scanLen);
                    }
                }
                
            } while (ras.navigateScanline(ras.scanlineStart() + 1));
        }
    }
    
    /**
     * Renders a single-style scanline directly to the renderer.
     */
    private static void renderScanlineSolid(
            ScanlineU8 sl,
            RendererBase renBase,
            Rgba8[] colors,
            int minX) {
        
        int y = sl.getY();
        int numSpans = sl.getNumSpans();
        
        for (int i = 0; i < numSpans; i++) {
            ScanlineU8.Span span = sl.getSpan(i);
            int x = span.x;
            int len = span.len;
            int[] covers = span.covers;
            
            if (covers != null) {
                // Span with coverage array
                for (int j = 0; j < len; j++) {
                    int alpha = covers[j];
                    if (alpha > 0) {
                        int colorIdx = x - minX + j;
                        // Bounds check
                        if (colorIdx >= 0 && colorIdx < colors.length) {
                            Rgba8 color = colors[colorIdx];
                            renBase.blendPixel(x + j, y, color, alpha);
                        }
                    }
                }
            } else {
                // Solid span
                for (int j = 0; j < len; j++) {
                    int colorIdx = x - minX + j;
                    // Bounds check
                    if (colorIdx >= 0 && colorIdx < colors.length) {
                        Rgba8 color = colors[colorIdx];
                        renBase.copyPixel(x + j, y, color);
                    }
                }
            }
        }
    }
    
    /**
     * Blends one style's coverage into the mix buffer.
     * Uses alpha blending for pixels where multiple styles overlap.
     */
    private static void blendStyleIntoMix(
            ScanlineU8 sl,
            Rgba8[] styleColors,
            Rgba8[] mixColors,
            int[] coverageBuffer,
            int minX) {
        
        int numSpans = sl.getNumSpans();
        
        for (int i = 0; i < numSpans; i++) {
            ScanlineU8.Span span = sl.getSpan(i);
            int x = span.x;
            int len = span.len;
            int[] covers = span.covers;
            
            for (int j = 0; j < len; j++) {
                int idx = x - minX + j;
                int alpha = (covers != null) ? covers[j] : 255;
                
                // Bounds check to prevent array index out of bounds
                if (idx < 0 || idx >= styleColors.length) {
                    continue;
                }
                
                if (alpha > 0) {
                    Rgba8 srcColor = styleColors[idx];
                    Rgba8 dstColor = mixColors[idx];
                    int prevCoverage = coverageBuffer[idx];
                    
                    if (prevCoverage == 0) {
                        // First style at this pixel - direct write to existing object
                        dstColor.r = srcColor.r;
                        dstColor.g = srcColor.g;
                        dstColor.b = srcColor.b;
                        dstColor.a = alpha;
                    } else {
                        // Multiple styles overlap - blend
                        int newAlpha = alpha + prevCoverage - (alpha * prevCoverage) / 255;
                        
                        if (newAlpha > 0) {
                            // Alpha blend
                            dstColor.r = (srcColor.r * alpha + dstColor.r * (255 - alpha)) / 255;
                            dstColor.g = (srcColor.g * alpha + dstColor.g * (255 - alpha)) / 255;
                            dstColor.b = (srcColor.b * alpha + dstColor.b * (255 - alpha)) / 255;
                            dstColor.a = newAlpha;
                        }
                    }
                    
                    coverageBuffer[idx] = Math.min(255, coverageBuffer[idx] + alpha);
                }
            }
        }
    }
    
    /**
     * Composites the blended mix buffer to the final renderer.
     */
    private static void compositeMixBuffer(
            Rgba8[] mixColors,
            int[] coverageBuffer,
            RendererBase renBase,
            int y,
            int minX,
            int len) {
        
        for (int i = 0; i < len; i++) {
            int coverage = coverageBuffer[i];
            if (coverage > 0) {
                Rgba8 color = mixColors[i];
                renBase.blendPixel(minX + i, y, color, coverage);
            }
        }
    }
}
