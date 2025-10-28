package agg;

/**
 * Main anti-aliased scanline rasterizer.
 * Converts vector paths to scanlines with coverage information for high-quality rendering.
 */
public class RasterizerScanlineAa {
    private RasterizerCellsAa cells;
    private int curX, curY;
    private boolean started;
    private int curScanY;
    private int curCellIndex;
    
    public RasterizerScanlineAa() {
        cells = new RasterizerCellsAa();
        curX = 0;
        curY = 0;
        started = false;
        curScanY = 0x7FFFFFFF;
        curCellIndex = 0;
    }
    
    public void reset() {
        cells.reset();
        started = false;
        curScanY = 0x7FFFFFFF;
        curCellIndex = 0;
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
        curScanY = minY();
        curCellIndex = 0;
        return cells.getSortedCells().size() > 0;
    }
    
    public boolean sweepScanline(ScanlineU8 sl) {
        // Check if we've processed all scanlines
        if (curScanY > maxY()) {
            return false;
        }
        
        sl.resetSpans();
        sl.setY(curScanY);
        
        int coverAccum = 0;
        java.util.List<CellAa> sortedCells = cells.getSortedCells();
        
        // Find cells for current scanline
        int startIdx = curCellIndex;
        while (curCellIndex < sortedCells.size()) {
            CellAa cell = sortedCells.get(curCellIndex);
            if (cell.y != curScanY) break;
            curCellIndex++;
        }
        
        // Process cells in this scanline
        if (curCellIndex > startIdx) {
            int prevX = sortedCells.get(startIdx).x;
            
            for (int i = startIdx; i < curCellIndex; i++) {
                CellAa cell = sortedCells.get(i);
                
                // Add span for gap if needed
                if (cell.x > prevX + 1 && coverAccum != 0) {
                    sl.addSpan(prevX + 1, cell.x - prevX - 1, 
                              AggBasics.calculateAlpha(coverAccum * AggBasics.POLY_SUBPIXEL_SCALE * 2));
                }
                
                // Add cell
                int area = cell.area;
                int cover = cell.cover;
                
                int alpha = AggBasics.calculateAlpha((coverAccum * AggBasics.POLY_SUBPIXEL_SCALE * 2 + area));
                if (alpha > 0) {
                    sl.addCell(cell.x, alpha);
                }
                
                coverAccum += cover;
                prevX = cell.x;
            }
            
            // Add final span if needed
            if (coverAccum != 0 && prevX + 1 <= maxX()) {
                sl.addSpan(prevX + 1, maxX() - prevX, 
                          AggBasics.calculateAlpha(coverAccum * AggBasics.POLY_SUBPIXEL_SCALE * 2));
            }
        }
        
        curScanY++;
        
        // Continue if we haven't reached the end
        return curScanY <= maxY();
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
