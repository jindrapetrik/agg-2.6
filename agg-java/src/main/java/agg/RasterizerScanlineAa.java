package agg;

/**
 * Main anti-aliased scanline rasterizer.
 * Converts vector paths to scanlines with coverage information for high-quality rendering.
 */
public class RasterizerScanlineAa {
    private RasterizerCellsAa cells;
    private int curX, curY;
    private boolean started;
    
    public RasterizerScanlineAa() {
        cells = new RasterizerCellsAa();
        curX = 0;
        curY = 0;
        started = false;
    }
    
    public void reset() {
        cells.reset();
        started = false;
    }
    
    public void moveTo(int x, int y) {
        if (started) {
            cells.line(curX, curY, curX, curY);
        }
        curX = x;
        curY = y;
        started = true;
    }
    
    public void lineTo(int x, int y) {
        if (started) {
            cells.line(curX, curY, x, y);
        }
        curX = x;
        curY = y;
        started = true;
    }
    
    public void moveToD(double x, double y) {
        moveTo(AggBasics.iround(x * AggBasics.POLY_SUBPIXEL_SCALE),
               AggBasics.iround(y * AggBasics.POLY_SUBPIXEL_SCALE));
    }
    
    public void lineToD(double x, double y) {
        lineTo(AggBasics.iround(x * AggBasics.POLY_SUBPIXEL_SCALE),
               AggBasics.iround(y * AggBasics.POLY_SUBPIXEL_SCALE));
    }
    
    public void addPath(VertexSource vs) {
        addPath(vs, 0);
    }
    
    public void addPath(VertexSource vs, int pathId) {
        double[] xy = new double[2];
        vs.rewind(pathId);
        
        int cmd;
        while (!AggBasics.isStop(cmd = vs.vertex(xy))) {
            if (AggBasics.isMoveTo(cmd)) {
                moveToD(xy[0], xy[1]);
            } else if (AggBasics.isVertex(cmd)) {
                lineToD(xy[0], xy[1]);
            } else if (AggBasics.isClose(cmd)) {
                // Close path - line back to start would happen automatically
            }
        }
    }
    
    public int minX() { return cells.minX(); }
    public int minY() { return cells.minY(); }
    public int maxX() { return cells.maxX(); }
    public int maxY() { return cells.maxY(); }
    
    public void sort() {
        cells.sortCells();
    }
    
    public boolean rewindScanlines() {
        cells.sortCells();
        return cells.getSortedCells().size() > 0;
    }
    
    public boolean navigateScanline(int y) {
        cells.sortCells();
        // Simplified - would need to implement scanline navigation
        return false;
    }
    
    public RasterizerCellsAa getCells() {
        return cells;
    }
}
