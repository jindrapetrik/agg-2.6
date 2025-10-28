//----------------------------------------------------------------------------
// Anti-Grain Geometry - Version 2.6 (Java 8 port)
// Example demonstrating the Arc class
//----------------------------------------------------------------------------

package agg.examples;

import agg.Arc;
import static agg.AggBasics.*;

/**
 * Simple example demonstrating the Arc class.
 */
public class ArcExample {
    
    public static void main(String[] args) {
        System.out.println("AGG Java - Arc Example");
        System.out.println("======================\n");
        
        // Create an arc centered at (100, 100) with radii 50x50
        // From 0 to PI/2 radians (0 to 90 degrees), counter-clockwise
        Arc arc = new Arc(100.0, 100.0, 50.0, 50.0, 
                         0.0, Math.PI / 2.0, true);
        
        System.out.println("Arc: center=(100, 100), rx=50, ry=50");
        System.out.println("     angle=0 to 90 degrees (counter-clockwise)\n");
        
        // Generate and display vertices
        arc.rewind(0);
        double[] xy = new double[2];
        int cmd;
        int vertexCount = 0;
        
        while (!isStop(cmd = arc.vertex(xy))) {
            String cmdName = isMoveTo(cmd) ? "MOVE_TO" : "LINE_TO";
            System.out.printf("Vertex %2d: x=%7.2f, y=%7.2f [%s]%n", 
                            vertexCount++, xy[0], xy[1], cmdName);
        }
        
        System.out.printf("%nTotal vertices generated: %d%n", vertexCount);
        
        // Demonstrate BSpline
        System.out.println("\n\nAGG Java - BSpline Example");
        System.out.println("==========================\n");
        
        demonstrateBSpline();
    }
    
    private static void demonstrateBSpline() {
        // This will be implemented when BSpline is fully working
        System.out.println("BSpline example - to be implemented");
        
        // Create sample data points
        double[] x = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {0.0, 1.0, 4.0, 2.0, 3.0, 5.0};
        
        System.out.println("Sample data points:");
        for (int i = 0; i < x.length; i++) {
            System.out.printf("  Point %d: x=%.1f, y=%.1f%n", i, x[i], y[i]);
        }
        
        System.out.println("\nBSpline interpolation would generate smooth curve through these points.");
        
        // Demonstrate Ellipse
        System.out.println("\n\nAGG Java - Ellipse Example");
        System.out.println("===========================\n");
        demonstrateEllipse();
        
        // Demonstrate RoundedRect
        System.out.println("\n\nAGG Java - RoundedRect Example");
        System.out.println("================================\n");
        demonstrateRoundedRect();
        
        // Demonstrate BezierArc
        System.out.println("\n\nAGG Java - BezierArc Example");
        System.out.println("==============================\n");
        demonstrateBezierArc();
    }
    
    private static void demonstrateEllipse() {
        agg.Ellipse ellipse = new agg.Ellipse(50.0, 50.0, 30.0, 20.0);
        System.out.println("Ellipse: center=(50, 50), rx=30, ry=20");
        
        ellipse.rewind(0);
        double[] xy = new double[2];
        int cmd;
        int count = 0;
        
        // Just show first 5 vertices
        while (count < 5 && !isStop(cmd = ellipse.vertex(xy))) {
            String cmdName = isMoveTo(cmd) ? "MOVE_TO" : "LINE_TO";
            System.out.printf("Vertex %2d: x=%6.2f, y=%6.2f [%s]%n", 
                            count++, xy[0], xy[1], cmdName);
        }
        System.out.println("... (more vertices follow)");
    }
    
    private static void demonstrateRoundedRect() {
        agg.RoundedRect rect = new agg.RoundedRect(10, 10, 90, 60, 5);
        System.out.println("RoundedRect: (10, 10) to (90, 60), radius=5");
        
        rect.rewind(0);
        double[] xy = new double[2];
        int cmd;
        int count = 0;
        
        // Show first 10 vertices
        while (count < 10 && !isStop(cmd = rect.vertex(xy))) {
            String cmdName = isMoveTo(cmd) ? "MOVE_TO" : 
                            isLineTo(cmd) ? "LINE_TO" : "OTHER";
            System.out.printf("Vertex %2d: x=%6.2f, y=%6.2f [%s]%n", 
                            count++, xy[0], xy[1], cmdName);
        }
        System.out.println("... (more vertices follow)");
    }
    
    private static void demonstrateBezierArc() {
        agg.BezierArc bezArc = new agg.BezierArc(50, 50, 30, 20, 0, Math.PI);
        System.out.println("BezierArc: center=(50, 50), rx=30, ry=20");
        System.out.println("           start=0, sweep=PI (half circle)");
        
        bezArc.rewind(0);
        double[] xy = new double[2];
        int cmd;
        int count = 0;
        
        // Show all vertices
        while (!isStop(cmd = bezArc.vertex(xy))) {
            String cmdName = isMoveTo(cmd) ? "MOVE_TO" : 
                            isCurve4(cmd) ? "CURVE4" : 
                            isLineTo(cmd) ? "LINE_TO" : "OTHER";
            System.out.printf("Vertex %2d: x=%6.2f, y=%6.2f [%s]%n", 
                            count++, xy[0], xy[1], cmdName);
        }
        System.out.printf("Total vertices: %d (produces bezier curves)%n", count);
    }
}
