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
     * Render a shape using the flash_rasterizer2 approach.
     * 
     * This method decomposes compound shapes into separate paths per fill style,
     * using the regular rasterizer with auto_close(false). For each fill style:
     * 1. Add all paths with leftFill matching the style (normal direction)
     * 2. Add all paths with rightFill matching the style (inverted direction)
     * 
     * This avoids the complexity of a full compound rasterizer while correctly
     * handling Flash edge-based fill definitions.
     * 
     * Returns the number of fills rendered.
     */
    private static int renderShapeWithFills(RendererBase renBase, CompoundShape shape, Rgba8[] colors) {
        // Create rasterizer and scanline
        RasterizerScanlineAa ras = new RasterizerScanlineAa();
        ScanlineU8 sl = new ScanlineU8();
        
        Transform2D scale = new Transform2D();
        ConvTransform trans = new ConvTransform(shape, scale);
        
        // Temporary path for inverting
        PathStorage tmpPath = new PathStorage();
        
        int fillsRendered = 0;
        
        // Use regular rasterizer in a mode that doesn't automatically close contours
        // This allows us to work with edge paths instead of closed polygons
        ras.autoClose(false);
        
        // Render each fill style from min to max
        int minS = shape.minStyle();
        int maxS = shape.maxStyle();
        System.out.println("  Rendering fills from " + minS + " to " + maxS);
        
        for (int s = minS; s <= maxS; s++) {
            ras.reset();
            int pathsAdded = 0;
            
            // For each path, check if it contributes to this fill style
            for (int i = 0; i < shape.paths(); i++) {
                PathStyle style = shape.style(i);
                
                // Only process paths where left and right fills are different
                // (paths with same left/right fill would add degenerate geometry)
                if (style.leftFill != style.rightFill) {
                    
                    // If this path has the fill on the LEFT side, add it normally
                    if (style.leftFill == s) {
                        ras.addPath(trans, style.pathId);
                        pathsAdded++;
                        System.out.println("    Fill " + s + ": Added path " + i + " (leftFill)");
                    }
                    
                    // If this path has the fill on the RIGHT side, add it inverted
                    if (style.rightFill == s) {
                        tmpPath.removeAll();
                        tmpPath.concatPath(trans, style.pathId);
                        tmpPath.invertPolygon(0);
                        ras.addPath(tmpPath, 0);
                        pathsAdded++;
                        System.out.println("    Fill " + s + ": Added path " + i + " (rightFill, inverted)");
                    }
                }
            }
            
            System.out.println("    Fill " + s + ": Total paths added = " + pathsAdded);
            
            // Ensure fillIdx is within color array bounds
            int colorIdx = s >= colors.length ? s % colors.length : s;
            Rgba8 color = colors[colorIdx];
            
            // Render this fill region
            RenderingScanlines.renderScanlines(ras, sl, renBase, color);
            fillsRendered++;
        }
        
        // Restore auto_close
        ras.autoClose(true);
        
        return fillsRendered;
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
