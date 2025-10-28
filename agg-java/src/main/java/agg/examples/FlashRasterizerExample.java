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
        
        // Process multiple shapes from file
        int shapeCount = 0;
        while (shape.readNext()) {
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
            
            // Only process first shape for rendering demo
            if (shapeCount == 1) {
                renderShape(shape);
            }
        }
        
        shape.close();
        System.out.println("\nTotal shapes processed: " + shapeCount);
        System.out.println("Done!");
    }
    
    /**
     * Render a shape to an image file.
     */
    private static void renderShape(CompoundShape shape) {
        System.out.println("\nRendering shape to image...");
        
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
        Rgba8 bgColor = new Rgba8(255, 255, 242); // Light yellow background
        rbuf.clear(bgColor);
        System.out.println("  Background cleared");
        
        // Simple rendering - draw path vertices as points
        renderSimple(rbuf, shape, colors);
        
        // Save to PPM file
        String outputFile = "flash_rasterizer_output.ppm";
        try {
            savePPM(rbuf, outputFile);
            System.out.println("  Output saved to: " + outputFile);
            System.out.println("  Image size: " + WIDTH + "x" + HEIGHT + " pixels");
            System.out.println("  You can view this file with image viewers that support PPM format");
        } catch (IOException e) {
            System.err.println("Error saving output: " + e.getMessage());
        }
    }
    
    /**
     * Simple rendering - mark path vertices with colored pixels.
     */
    private static void renderSimple(RenderingBuffer rbuf, CompoundShape shape, Rgba8[] colors) {
        Transform2D scale = new Transform2D();
        ConvTransform trans = new ConvTransform(shape, scale);
        
        int totalVertices = 0;
        
        // Draw each path
        for (int i = 0; i < shape.paths(); i++) {
            PathStyle style = shape.style(i);
            
            // Get color for this path
            int fillIdx = getFillIndex(style, colors.length);
            Rgba8 color = colors[fillIdx];
            
            // Iterate through vertices
            trans.rewind(style.pathId);
            double[] xy = new double[2];
            int cmd;
            
            while (!AggBasics.isStop(cmd = trans.vertex(xy))) {
                if (AggBasics.isVertex(cmd)) {
                    int x = (int) xy[0];
                    int y = (int) xy[1];
                    
                    // Draw a small marker at each vertex
                    drawVertexMarker(rbuf, x, y, color);
                    totalVertices++;
                }
            }
        }
        
        System.out.println("  Rendered " + totalVertices + " vertices");
    }
    
    /**
     * Get fill index from path style.
     */
    private static int getFillIndex(PathStyle style, int maxColors) {
        int fillIdx = style.leftFill >= 0 ? style.leftFill : 
                     (style.rightFill >= 0 ? style.rightFill : 0);
        if (fillIdx < 0 || fillIdx >= maxColors) {
            fillIdx = 0;
        }
        return fillIdx;
    }
    
    /**
     * Draw a small cross marker at a vertex.
     */
    private static void drawVertexMarker(RenderingBuffer rbuf, int x, int y, Rgba8 color) {
        // Draw a 3x3 marker for visibility
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                rbuf.setPixel(x + dx, y + dy, color);
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
