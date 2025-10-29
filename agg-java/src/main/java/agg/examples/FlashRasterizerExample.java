package agg.examples;

import agg.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Flash Rasterizer Example
 * Demonstrates compound shape rendering with fills and strokes.
 * Java translation of flash_rasterizer.cpp
 */
public class FlashRasterizerExample {
    
    private static final int WIDTH = 655;
    private static final int HEIGHT = 520;
    
    public static void main(String[] args) {
        System.out.println("AGG Java - Flash Rasterizer Example");
        System.out.println("=====================================\n");
        
        // Find shapes file
        String shapesFile = args.length > 0 ? args[0] : findShapesFile();
        if (shapesFile == null) {
            System.err.println("Error: Could not find shapes.txt file");
            System.err.println("Please specify path as command line argument or ensure it's in resources");
            return;
        }
        
        System.out.println("Loading shapes from: " + shapesFile);
        
        // Create compound shape and load data
        CompoundShape shape = new CompoundShape();
        if (!shape.open(shapesFile)) {
            System.err.println("Error: Could not open shapes file: " + shapesFile);
            return;
        }
        
        // Create rendering buffer and pixel format for first shape only
        RenderingBuffer rbuf = new RenderingBuffer(WIDTH, HEIGHT, 4);
        PixFmtRgba pixf = new PixFmtRgba(rbuf);
        RendererBase renBase = new RendererBase(pixf);
        
        // Clear background
        Rgba8 bgColor = new Rgba8(255, 255, 242); // Light yellow background
        renBase.clear(bgColor);
        System.out.println("\nBackground cleared");
        
        // Create random colors for fills
        Rgba8[] colors = new Rgba8[100];
        Random rand = new Random(12345); // Fixed seed for reproducibility
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Rgba8(
                rand.nextInt(256),
                rand.nextInt(256),
                rand.nextInt(256),
                230
            );
            colors[i] = colors[i].premultiply();
        }
        
        // Process and render ONLY THE FIRST shape from file
        int shapeCount = 0;
        int totalPathsRendered = 0;
        if (shape.readNext()) {
            shapeCount++;
            System.out.println("\nShape #" + shapeCount + ":");
            System.out.println("  Paths: " + shape.paths());
            
            // Show some path information
            for (int i = 0; i < Math.min(5, shape.paths()); i++) {
                PathStyle style = shape.style(i);
                System.out.printf("  Path %d: left_fill=%d, right_fill=%d, line=%d%n",
                    i, style.leftFill, style.rightFill, style.line);
            }
            if (shape.paths() > 5) {
                System.out.println("  ... (" + (shape.paths() - 5) + " more paths)");
            }
            
            // Scale shape to fit window
            shape.scale(WIDTH, HEIGHT);
            System.out.printf("  Scaled to fit %dx%d window (scale factor: %.2f)%n", 
                WIDTH, HEIGHT, shape.getScale());
            
            // Render this shape using left/right fills
            int fillsRendered = renderShapeWithFills(renBase, shape, colors);
            totalPathsRendered += fillsRendered;
            System.out.println("  Rendered " + fillsRendered + " fill regions");
        }
        
        shape.close();
        
        // Save the output
        System.out.println("\nSaving output...");
        String outputFile = "flash_rasterizer_output.ppm";
        try {
            savePPM(rbuf, outputFile);
            System.out.println("Output saved to: " + outputFile);
            System.out.println("Image size: " + WIDTH + "x" + HEIGHT + " pixels");
            System.out.println("Total shapes rendered: " + shapeCount);
            System.out.println("Total fill regions rendered: " + totalPathsRendered);
        } catch (IOException e) {
            System.err.println("Error saving output: " + e.getMessage());
        }
        
        System.out.println("\nDone!");
    }
    
    /**
     * Render a shape using RenderScanlinesCompound (matching C++ flash_rasterizer.cpp).
     * 
     * This method uses the compound rasterizer which correctly handles overlapping fills
     * and provides perfect polygon stitching at boundaries. It matches the implementation
     * in the C++ version's flash_rasterizer.cpp.
     * 
     * Returns the number of fills rendered.
     */
    private static int renderShapeWithFills(RendererBase renBase, CompoundShape shape, Rgba8[] colors) {
        // Create compound rasterizer, scanlines, and allocator
        RasterizerCompoundAa rasc = new RasterizerCompoundAa();
        ScanlineU8 sl = new ScanlineU8();
        ScanlineBin slBin = new ScanlineBin();
        SpanAllocator alloc = new SpanAllocator();
        
        Transform2D scale = new Transform2D();
        ConvTransform trans = new ConvTransform(shape, scale);
        
        // Create style handler for color mapping
        TestStyles styleHandler = new TestStyles(colors);
        
        // Setup rasterizer
        rasc.clipBox(0, 0, WIDTH, HEIGHT);
        rasc.reset();
        
        System.out.println("  Adding paths to compound rasterizer:");
        
        // Add all paths with their left/right fill styles
        for (int i = 0; i < shape.paths(); i++) {
            PathStyle style = shape.style(i);
            
            if (style.leftFill >= 0 || style.rightFill >= 0) {
                // Set the left and right fill styles for this path
                rasc.styles(style.leftFill, style.rightFill);
                
                // Add the path to the compound rasterizer
                rasc.addPath(trans, style.pathId);
                
                System.out.printf("    Path %d: left_fill=%d, right_fill=%d%n",
                    i, style.leftFill, style.rightFill);
            }
        }
        
        // Render using compound scanline renderer
        System.out.println("  Rendering with compound rasterizer...");
        RenderScanlinesCompound.renderScanlinesCompound(rasc, sl, slBin, renBase, alloc, styleHandler);
        
        return shape.paths();
    }
    
    /**
     * Style handler for compound rasterization.
     * Maps fill style indices to colors (similar to test_styles in C++ version).
     */
    private static class TestStyles implements StyleHandler {
        private final Rgba8[] solidColors;
        
        public TestStyles(Rgba8[] colors) {
            this.solidColors = colors;
        }
        
        @Override
        public boolean isSolid(int style) {
            return true;  // All styles are solid colors
        }
        
        @Override
        public Rgba8 color(int style) {
            // Get color for this style (with bounds checking)
            int colorIdx = style >= solidColors.length ? style % solidColors.length : style;
            if (colorIdx < 0) colorIdx = 0;
            return solidColors[colorIdx];
        }
        
        @Override
        public void generateSpan(Rgba8[] span, int x, int y, int len, int style) {
            // Get color for this style (with bounds checking)
            Rgba8 color = color(style);
            
            // Fill the span with the solid color
            for (int i = 0; i < len; i++) {
                if (i < span.length) {
                    span[i] = new Rgba8(color.r, color.g, color.b, color.a);
                }
            }
        }
    }
    
    /**
     * Find shapes.txt file in various locations.
     */
    private static String findShapesFile() {
        // Try to extract from resources
        try {
            InputStream is = FlashRasterizerExample.class.getResourceAsStream("/shapes.txt");
            if (is != null) {
                File tempFile = File.createTempFile("shapes", ".txt");
                tempFile.deleteOnExit();
                
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                is.close();
                
                return tempFile.getAbsolutePath();
            }
        } catch (IOException e) {
            // Ignore and try other locations
        }
        
        // Try current directory
        if (new File("shapes.txt").exists()) {
            return "shapes.txt";
        }
        
        // Try resources directory
        if (new File("src/main/resources/shapes.txt").exists()) {
            return "src/main/resources/shapes.txt";
        }
        
        return null;
    }
    
    /**
     * Save rendering buffer to PPM file (simple uncompressed format).
     */
    private static void savePPM(RenderingBuffer rbuf, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        
        // Write PPM header
        String header = String.format("P6\n%d %d\n255\n", rbuf.width(), rbuf.height());
        fos.write(header.getBytes());
        
        // Write pixel data (convert BGRA to RGB)
        byte[] buffer = rbuf.buffer();
        byte[] rgb = new byte[3];
        
        for (int y = 0; y < rbuf.height(); y++) {
            for (int x = 0; x < rbuf.width(); x++) {
                int offset = y * rbuf.stride() + x * 4;
                rgb[0] = buffer[offset + 2]; // R
                rgb[1] = buffer[offset + 1]; // G
                rgb[2] = buffer[offset];     // B
                fos.write(rgb);
            }
        }
        
        fos.close();
    }
}
