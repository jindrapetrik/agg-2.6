import agg.*;

public class DebugSingleTriangle {
    public static void main(String[] args) {
        PathStorage path = new PathStorage();
        
        // Simulate the single triangle
        int pathId = path.startNewPath();
        System.out.println("Path ID: " + pathId);
        
        path.moveTo(0, 0);
        path.lineTo(50, 50);
        path.lineTo(0, 50);
        path.lineTo(0, 0);
        
        System.out.println("Total vertices before inversion: " + path.totalVertices());
        
        // Create tmp path and concat
        PathStorage tmpPath = new PathStorage();
        tmpPath.concatPath(path, pathId);
        
        System.out.println("tmpPath vertices after concat: " + tmpPath.totalVertices());
        
        // Trace before inversion
        System.out.println("\nBefore inversion:");
        tmpPath.rewind(0);
        double[] vertex = new double[2];
        int cmd;
        int count = 0;
        while (!AggBasics.isStop(cmd = tmpPath.vertex(vertex))) {
            System.out.println("  " + count + ": cmd=" + cmd + ", x=" + vertex[0] + ", y=" + vertex[1]);
            count++;
        }
        
        // Invert
        tmpPath.invertPolygon(0);
        
        System.out.println("\nAfter inversion:");
        System.out.println("tmpPath vertices: " + tmpPath.totalVertices());
        tmpPath.rewind(0);
        count = 0;
        while (!AggBasics.isStop(cmd = tmpPath.vertex(vertex))) {
            System.out.println("  " + count + ": cmd=" + cmd + ", x=" + vertex[0] + ", y=" + vertex[1]);
            count++;
        }
        
        // Now test with rasterizer
        System.out.println("\n\nTesting with rasterizer:");
        RasterizerScanlineAa ras = new RasterizerScanlineAa();
        ras.autoClose(false);
        ras.addPath(tmpPath, 0);
        
        System.out.println("Rasterizer ready: " + ras.rewindScanlines());
        
        ScanlineU8 sl = new ScanlineU8();
        int scanlineCount = 0;
        while (ras.sweepScanline(sl)) {
            scanlineCount++;
        }
        System.out.println("Total scanlines: " + scanlineCount);
    }
}
