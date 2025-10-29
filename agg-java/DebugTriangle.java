import agg.*;
import java.util.ArrayList;

public class DebugTriangle {
    public static void main(String[] args) {
        PathStorage path = new PathStorage();
        
        // Simulate what the parser does for our triangle
        int pathId = path.startNewPath();
        System.out.println("Path ID after startNewPath: " + pathId);
        
        path.moveTo(0, 0);
        System.out.println("After moveTo(0,0): commands=" + path.totalVertices());
        
        path.lineTo(50, 50);
        System.out.println("After lineTo(50,50): commands=" + path.totalVertices());
        
        path.lineTo(0, 50);
        System.out.println("After lineTo(0,50): commands=" + path.totalVertices());
        
        path.lineTo(0, 0);
        System.out.println("After lineTo(0,0): commands=" + path.totalVertices());
        
        // Now let's trace through the path
        System.out.println("\nTracing path from pathId=" + pathId + ":");
        path.rewind(pathId);
        double[] vertex = new double[2];
        int cmd;
        int count = 0;
        while (!AggBasics.isStop(cmd = path.vertex(vertex))) {
            System.out.println("  Vertex " + count + ": cmd=" + cmd + ", x=" + vertex[0] + ", y=" + vertex[1]);
            count++;
        }
        
        // Now invert the polygon
        System.out.println("\nInverting polygon...");
        path.invertPolygon(pathId, path.totalVertices());
        
        System.out.println("\nTracing inverted path from pathId=" + pathId + ":");
        path.rewind(pathId);
        count = 0;
        while (!AggBasics.isStop(cmd = path.vertex(vertex))) {
            System.out.println("  Vertex " + count + ": cmd=" + cmd + ", x=" + vertex[0] + ", y=" + vertex[1]);
            count++;
        }
    }
}
