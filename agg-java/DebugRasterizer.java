import agg.*;

public class DebugRasterizer {
    public static void main(String[] args) {
        RasterizerScanlineAa ras = new RasterizerScanlineAa();
        ras.autoClose(false);
        
        System.out.println("Before adding path:");
        System.out.println("  started = " + getStarted(ras));
        System.out.println("  minX/maxX = " + ras.minX() + "/" + ras.maxX());
        System.out.println("  minY/maxY = " + ras.minY() + "/" + ras.maxY());
        
        // Add a simple triangle: LINE_TO, LINE_TO, MOVE_TO (inverted form)
        ras.lineToD(0, 50);
        System.out.println("\nAfter first LINE_TO(0, 50):");
        System.out.println("  started = " + getStarted(ras));
        System.out.println("  minX/maxX = " + ras.minX() + "/" + ras.maxX());
        
        ras.lineToD(50, 50);
        System.out.println("\nAfter second LINE_TO(50, 50):");
        System.out.println("  started = " + getStarted(ras));
        System.out.println("  minX/maxX = " + ras.minX() + "/" + ras.maxX());
        
        ras.moveToD(0, 0);
        System.out.println("\nAfter MOVE_TO(0, 0):");
        System.out.println("  started = " + getStarted(ras));
        System.out.println("  minX/maxX = " + ras.minX() + "/" + ras.maxX());
        
        boolean ready = ras.rewindScanlines();
        System.out.println("\nRasterizer ready: " + ready);
        System.out.println("  minX/maxX = " + ras.minX() + "/" + ras.maxX());
        System.out.println("  minY/maxY = " + ras.minY() + "/" + ras.maxY());
        
        ScanlineU8 sl = new ScanlineU8();
        int count = 0;
        while (ras.sweepScanline(sl)) {
            count++;
        }
        System.out.println("Total scanlines: " + count);
    }
    
    private static boolean getStarted(RasterizerScanlineAa ras) {
        // We can't access private field, so just return unknown
        return false; // Can't access
    }
}
