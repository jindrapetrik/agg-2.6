package agg.examples;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Simple utility to convert PPM files to PNG format.
 */
public class PPMtoPNG {
    
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: PPMtoPNG <input.ppm> <output.png>");
            return;
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        
        System.out.println("Converting " + inputFile + " to " + outputFile);
        
        // Read PPM
        DataInputStream dis = new DataInputStream(new FileInputStream(inputFile));
        
        // Read header
        String magic = readLine(dis);
        if (!magic.equals("P6")) {
            System.err.println("Not a binary PPM file (P6 format)");
            return;
        }
        
        // Skip comments
        String line;
        do {
            line = readLine(dis);
        } while (line.startsWith("#"));
        
        // Parse dimensions
        String[] dims = line.split("\\s+");
        int width = Integer.parseInt(dims[0]);
        int height = Integer.parseInt(dims[1]);
        
        // Read max value
        String maxVal = readLine(dis);
        
        System.out.println("Image: " + width + "x" + height);
        
        // Read pixel data
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        byte[] rgb = new byte[3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dis.readFully(rgb);
                int r = rgb[0] & 0xFF;
                int g = rgb[1] & 0xFF;
                int b = rgb[2] & 0xFF;
                int color = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, color);
            }
        }
        
        dis.close();
        
        // Write PNG
        ImageIO.write(image, "PNG", new File(outputFile));
        
        System.out.println("Conversion complete!");
    }
    
    private static String readLine(DataInputStream dis) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = dis.read()) != '\n' && ch != -1) {
            if (ch != '\r') {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }
}
