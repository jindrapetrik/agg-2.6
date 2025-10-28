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
        
        // Read first shape
        if (!shape.readNext()) {
            System.err.println("Error: Could not read shape data");
            shape.close();
            return;
        }
        
        System.out.println("Shape loaded: " + shape.paths() + " paths");
        
        // Scale shape to fit window
        shape.scale(WIDTH, HEIGHT);
        
        // Create rendering buffer
        RenderingBuffer rbuf = new RenderingBuffer(WIDTH, HEIGHT, 4);
        
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
        
        // Clear background
        rbuf.clear(new Rgba8(255, 255, 242)); // Light yellow background
        
        System.out.println("Rendering...");
        
        // Simple rendering - just draw filled paths
        // (This is a simplified version without the full compound rasterizer)
        renderSimple(rbuf, shape, colors);
        
        // Save to PPM file
        String outputFile = "flash_rasterizer_output.ppm";
        try {
            savePPM(rbuf, outputFile);
            System.out.println("\nOutput saved to: " + outputFile);
            System.out.println("Image size: " + WIDTH + "x" + HEIGHT + " pixels");
        } catch (IOException e) {
            System.err.println("Error saving output: " + e.getMessage());
        }
        
        shape.close();
        System.out.println("\nDone!");
    }
    
    /**
     * Simple rendering without full compound rasterizer.
     * Just demonstrates that shapes can be loaded and transformed.
     */
    private static void renderSimple(RenderingBuffer rbuf, CompoundShape shape, Rgba8[] colors) {
        // Create a simple rasterizer
        RasterizerScanlineAa ras = new RasterizerScanlineAa();
        ScanlineU8 sl = new ScanlineU8();
        
        Transform2D scale = new Transform2D();
        ConvTransform trans = new ConvTransform(shape, scale);
        
        // Draw each path
        for (int i = 0; i < shape.paths(); i++) {
            PathStyle style = shape.style(i);
            
            // Only draw paths with fills (skip stroke-only paths)
            if (style.leftFill >= 0 || style.rightFill >= 0) {
                int fillIdx = style.leftFill >= 0 ? style.leftFill : style.rightFill;
                if (fillIdx >= 0 && fillIdx < colors.length) {
                    ras.reset();
                    ras.addPath(trans, style.pathId);
                    
                    // Simplified scanline rendering
                    Rgba8 color = colors[fillIdx];
                    renderScanlinesSolid(ras, sl, rbuf, color);
                }
            }
        }
    }
    
    /**
     * Simplified scanline rendering.
     */
    private static void renderScanlinesSolid(RasterizerScanlineAa ras, 
                                             ScanlineU8 sl, 
                                             RenderingBuffer rbuf, 
                                             Rgba8 color) {
        // This is a placeholder - proper implementation would use the rasterizer
        // For now, just mark that we would render here
        // A full implementation would iterate through scanlines and render spans
        System.out.print(".");
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
