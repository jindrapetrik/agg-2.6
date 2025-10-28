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
            int pathsRendered = renderShapeWithFills(renBase, shape, colors);
            totalPathsRendered += pathsRendered;
            System.out.println("  Rendered " + pathsRendered + " paths with fills");
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
            System.out.println("Total paths rendered: " + totalPathsRendered);
        } catch (IOException e) {
            System.err.println("Error saving output: " + e.getMessage());
        }
        
        System.out.println("\nDone!");
    }
    
    /**
     * Render a shape with proper left/right fill handling.
     * Returns the number of paths rendered.
     */
    private static int renderShapeWithFills(RendererBase renBase, CompoundShape shape, Rgba8[] colors) {
        // Create rasterizer and scanline
        RasterizerScanlineAa ras = new RasterizerScanlineAa();
        ScanlineU8 sl = new ScanlineU8();
        
        Transform2D scale = new Transform2D();
        ConvTransform trans = new ConvTransform(shape, scale);
        
        int pathsRendered = 0;
        
        // Render each path - use leftFill and rightFill to determine color
        for (int i = 0; i < shape.paths(); i++) {
            PathStyle style = shape.style(i);
            
            // Determine which fill to use (prefer leftFill, fallback to rightFill)
            int fillIdx = -1;
            if (style.leftFill >= 0) {
                fillIdx = style.leftFill;
            } else if (style.rightFill >= 0) {
                fillIdx = style.rightFill;
            }
            
            // Only render paths that have a fill
            if (fillIdx >= 0) {
                // Ensure fillIdx is within color array bounds
                if (fillIdx >= colors.length) {
                    fillIdx = fillIdx % colors.length;
                }
                
                Rgba8 color = colors[fillIdx];
                
                // Add path to rasterizer
                ras.reset();
                ras.addPath(trans, style.pathId);
                
                // Render scanlines
                RenderingScanlines.renderScanlines(ras, sl, renBase, color);
                pathsRendered++;
            }
        }
        
        return pathsRendered;
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
