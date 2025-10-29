import agg.*;

public class DebugInversion {
    public static void main(String[] args) {
        // Simulate the first triangle
        PathStorage path = new PathStorage();
        path.moveTo(0, 0);
        path.lineTo(50, 50);
        path.lineTo(0, 50);
        path.lineTo(0, 0);  // Explicit close
        
        System.out.println("Original path:");
        printPath(path);
        
        System.out.println("\nInverting polygon...");
        path.invertPolygon(0);
        
        System.out.println("\nInverted path:");
        printPath(path);
    }
    
    static void printPath(PathStorage path) {
        path.rewind(0);
        double[] xy = new double[2];
        int cmd;
        int count = 0;
        while (!AggBasics.isStop(cmd = path.vertex(xy))) {
            String cmdName = AggBasics.isMoveTo(cmd) ? "MOVE_TO" :
                           AggBasics.isLineTo(cmd) ? "LINE_TO" :
                           AggBasics.isClose(cmd) ? "CLOSE" : "VERTEX";
            System.out.printf("  %d: %s (%.1f, %.1f)%n", count++, cmdName, xy[0], xy[1]);
        }
        System.out.println("  Total: " + count + " vertices");
    }
}
