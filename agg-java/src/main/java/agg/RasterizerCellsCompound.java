package agg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Rasterizer cells for compound anti-aliased rendering with style tracking.
 * This extends the basic cell rasterization with left/right style information.
 * Based on rasterizer_cells_aa<cell_style_aa> from C++ AGG.
 */
public class RasterizerCellsCompound {
    private static final int CELL_BLOCK_SHIFT = 12;
    private static final int CELL_BLOCK_SIZE = 1 << CELL_BLOCK_SHIFT;
    private static final int CELL_BLOCK_MASK = CELL_BLOCK_SIZE - 1;
    private static final int CELL_BLOCK_POOL = 256;
    private static final int CELL_BLOCK_LIMIT = 1024;
    
    private List<CellStyleAa[]> cells;
    private List<CellStyleAa> sortedCells;
    private CellStyleAa currentCell;
    private CellStyleAa styleCell;  // Current style for new cells
    private int minX, minY, maxX, maxY;
    private boolean sorted;
    private int curX, curY;
    private int coverAccum;
    private int areaAccum;
    private int cellBlockLimit;
    
    public RasterizerCellsCompound() {
        this(CELL_BLOCK_LIMIT);
    }
    
    public RasterizerCellsCompound(int cellBlockLimit) {
        this.cellBlockLimit = cellBlockLimit;
        cells = new ArrayList<>();
        sortedCells = new ArrayList<>();
        currentCell = new CellStyleAa();
        styleCell = new CellStyleAa();
        reset();
    }
    
    public void reset() {
        cells.clear();
        sortedCells.clear();
        sorted = false;
        minX = 0x7FFFFFFF;
        minY = 0x7FFFFFFF;
        maxX = -0x7FFFFFFF;
        maxY = -0x7FFFFFFF;
        curX = 0;
        curY = 0;
        coverAccum = 0;
        areaAccum = 0;
        currentCell.initial();
        styleCell.initial();
    }
    
    public void resetClipping() {
        // For now, just reset - clipping can be added later if needed
        reset();
    }
    
    public void clipBox(double x1, double y1, double x2, double y2) {
        // Basic clipping - for full implementation would need clipper
        reset();
    }
    
    /**
     * Set the current style for new cells.
     * Returns the style cell for modification.
     */
    public CellStyleAa style() {
        return styleCell;
    }
    
    public void moveTo(int x, int y) {
        // Close current cell if it has coverage
        if ((currentCell.area | currentCell.cover) != 0) {
            addCurrentCell();
        }
        
        // Set current position
        curX = x;
        curY = y;
        setCurrentCell(x >> AggBasics.POLY_SUBPIXEL_SHIFT, 
                       y >> AggBasics.POLY_SUBPIXEL_SHIFT);
    }
    
    public void lineTo(int x, int y) {
        line(curX, curY, x, y);
        curX = x;
        curY = y;
    }
    
    public void line(int x1, int y1, int x2, int y2) {
        // Limit for subdividing long lines to avoid overflow
        final int DX_LIMIT = 16384 << AggBasics.POLY_SUBPIXEL_SHIFT;
        
        long dx = (long)x2 - (long)x1;
        
        // If line is too long, subdivide it
        if (dx >= DX_LIMIT || dx <= -DX_LIMIT) {
            int cx = (int)(((long)x1 + (long)x2) >> 1);
            int cy = (int)(((long)y1 + (long)y2) >> 1);
            line(x1, y1, cx, cy);
            line(cx, cy, x2, y2);
            return;
        }
        
        long dy = (long)y2 - (long)y1;
        int ex1 = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ex2 = x2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ey1 = y1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ey2 = y2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int fy1 = y1 & AggBasics.POLY_SUBPIXEL_MASK;
        int fy2 = y2 & AggBasics.POLY_SUBPIXEL_MASK;
        
        // Update bounds
        if (ex1 < minX) minX = ex1;
        if (ex1 > maxX) maxX = ex1;
        if (ey1 < minY) minY = ey1;
        if (ey1 > maxY) maxY = ey1;
        if (ex2 < minX) minX = ex2;
        if (ex2 > maxX) maxX = ex2;
        if (ey2 < minY) minY = ey2;
        if (ey2 > maxY) maxY = ey2;
        
        setCurrentCell(ex1, ey1);
        
        // Everything is on a single horizontal line
        if (ey1 == ey2) {
            renderHLine(ey1, x1, fy1, x2, fy2);
            return;
        }
        
        // Vertical or diagonal line
        int incr = 1;
        if (dx == 0) {
            // Vertical line
            int ex = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
            int twoFx = (x1 - (ex << AggBasics.POLY_SUBPIXEL_SHIFT)) << 1;
            int first = AggBasics.POLY_SUBPIXEL_SCALE;
            if (dy < 0) {
                first = 0;
                incr = -1;
            }
            
            int delta;
            int from, to;
            from = fy1;
            to = fy2;
            
            if (dy < 0) {
                int tmp = from;
                from = to;
                to = tmp;
            }
            
            delta = to - from;
            currentCell.cover += delta * incr;
            currentCell.area += (from + to) * delta * incr;
            
            ey1 += incr;
            setCurrentCell(ex, ey1);
            
            delta = first - from;
            currentCell.cover += delta * incr;
            currentCell.area += (from + first) * delta * incr;
            
            fy1 = first;
            
            while (ey1 != ey2) {
                currentCell.cover += AggBasics.POLY_SUBPIXEL_SCALE * incr;
                currentCell.area += (AggBasics.POLY_SUBPIXEL_SCALE * 2) * incr;
                ey1 += incr;
                setCurrentCell(ex, ey1);
                fy1 = first;
            }
            
            delta = fy2 - AggBasics.POLY_SUBPIXEL_SCALE + first;
            currentCell.cover += delta * incr;
            currentCell.area += (AggBasics.POLY_SUBPIXEL_SCALE + fy2) * delta * incr;
            return;
        }
        
        // General case - Bresenham-like line rendering
        long p;
        int first = AggBasics.POLY_SUBPIXEL_SCALE;
        int lift, delta, mod, rem;
        
        if (dy < 0) {
            p = (long)((long)first * dx / dy);
            first = 0;
            incr = -1;
            dy = -dy;
        } else {
            p = (long)(((long)(AggBasics.POLY_SUBPIXEL_SCALE - first) * dx) / dy);
        }
        
        int x_from = x1;
        delta = (int)p;
        
        for (;;) {
            int fx1 = x_from & AggBasics.POLY_SUBPIXEL_MASK;
            int fx2 = fx1 + delta;
            int ex = x_from >> AggBasics.POLY_SUBPIXEL_SHIFT;
            
            if ((ey1 - ey2) * incr >= 0) break;
            
            int area_delta = (fx1 + fx2) * (first - fy1);
            currentCell.cover += (first - fy1) * incr;
            currentCell.area += area_delta * incr;
            fy1 = first;
            
            ey1 += incr;
            setCurrentCell(ex, ey1);
            
            if (fx2 >= AggBasics.POLY_SUBPIXEL_SCALE) {
                int fx = AggBasics.POLY_SUBPIXEL_SCALE;
                delta = fx2 - fx;
                area_delta = (fx1 + fx) * (first - fy1);
                currentCell.cover += (first - fy1) * incr;
                currentCell.area += area_delta * incr;
                
                x_from += AggBasics.POLY_SUBPIXEL_SCALE;
                setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
            }
            
            p = (long)((long)AggBasics.POLY_SUBPIXEL_SCALE * dx / dy);
            delta = (int)p;
            x_from += delta;
        }
        
        delta = fy2 - fy1;
        currentCell.cover += delta * incr;
        currentCell.area += (fy1 + fy2) * delta * incr;
    }
    
    private void renderHLine(int ey, int x1, int y1, int x2, int y2) {
        int ex1 = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ex2 = x2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int fx1 = x1 & AggBasics.POLY_SUBPIXEL_MASK;
        int fx2 = x2 & AggBasics.POLY_SUBPIXEL_MASK;
        
        int delta;
        
        if (y1 == y2) {
            setCurrentCell(ex2, ey);
            return;
        }
        
        // Everything is within the same cell
        if (ex1 == ex2) {
            delta = y2 - y1;
            currentCell.cover += delta;
            currentCell.area += (fx1 + fx2) * delta;
            return;
        }
        
        // Horizontal line spans multiple cells
        int p, first, incr;
        long dx = (long)x2 - (long)x1;
        
        if (dx > 0) {
            p = (int)(((long)(AggBasics.POLY_SUBPIXEL_SCALE - fx1) * (long)(y2 - y1)) / dx);
            first = AggBasics.POLY_SUBPIXEL_SCALE;
            incr = 1;
        } else {
            p = (int)(((long)fx1 * (long)(y2 - y1)) / -dx);
            first = 0;
            incr = -1;
            dx = -dx;
        }
        
        delta = p - y1;
        currentCell.cover += delta;
        currentCell.area += (fx1 + first) * delta;
        
        ex1 += incr;
        setCurrentCell(ex1, ey);
        y1 = p;
        
        if (ex1 != ex2) {
            p = (int)(((long)AggBasics.POLY_SUBPIXEL_SCALE * (long)(y2 - y1)) / dx);
            while (ex1 != ex2) {
                delta = p;
                currentCell.cover += delta;
                currentCell.area += AggBasics.POLY_SUBPIXEL_SCALE * delta;
                y1 += p;
                ex1 += incr;
                setCurrentCell(ex1, ey);
            }
        }
        
        delta = y2 - y1;
        currentCell.cover += delta;
        currentCell.area += (fx2 + first) * delta;
    }
    
    private void setCurrentCell(int x, int y) {
        if (currentCell.notEqual(x, y, styleCell) != 0) {
            addCurrentCell();
            currentCell.style(styleCell);
            currentCell.x = x;
            currentCell.y = y;
            currentCell.cover = 0;
            currentCell.area = 0;
        }
    }
    
    private void addCurrentCell() {
        if ((currentCell.area | currentCell.cover) != 0) {
            int blockNum = cells.size();
            if (blockNum >= cellBlockLimit) {
                return; // Limit reached
            }
            
            // Allocate new block if needed
            if (sortedCells.size() >= blockNum * CELL_BLOCK_SIZE) {
                cells.add(new CellStyleAa[CELL_BLOCK_SIZE]);
                for (int i = 0; i < CELL_BLOCK_SIZE; i++) {
                    cells.get(cells.size() - 1)[i] = new CellStyleAa();
                }
            }
            
            // Add cell to sorted list
            int idx = sortedCells.size();
            int block = idx / CELL_BLOCK_SIZE;
            int offset = idx % CELL_BLOCK_SIZE;
            
            if (block >= cells.size()) {
                cells.add(new CellStyleAa[CELL_BLOCK_SIZE]);
                for (int i = 0; i < CELL_BLOCK_SIZE; i++) {
                    cells.get(cells.size() - 1)[i] = new CellStyleAa();
                }
            }
            
            CellStyleAa cell = cells.get(block)[offset];
            cell.x = currentCell.x;
            cell.y = currentCell.y;
            cell.cover = currentCell.cover;
            cell.area = currentCell.area;
            cell.left = currentCell.left;
            cell.right = currentCell.right;
            
            sortedCells.add(cell);
            sorted = false;
        }
    }
    
    public int minX() { return minX; }
    public int minY() { return minY; }
    public int maxX() { return maxX; }
    public int maxY() { return maxY; }
    public boolean sorted() { return sorted; }
    
    public int totalCells() {
        return sortedCells.size();
    }
    
    public void sortCells() {
        if (sorted) return;
        
        // Add current cell if it has coverage
        addCurrentCell();
        currentCell.initial();
        
        if (sortedCells.isEmpty()) {
            sorted = true;
            return;
        }
        
        // Sort cells by Y then X
        Collections.sort(sortedCells, new Comparator<CellStyleAa>() {
            @Override
            public int compare(CellStyleAa a, CellStyleAa b) {
                if (a.y < b.y) return -1;
                if (a.y > b.y) return 1;
                if (a.x < b.x) return -1;
                if (a.x > b.x) return 1;
                return 0;
            }
        });
        
        sorted = true;
    }
    
    public int scanlineNumCells(int y) {
        if (y < minY || y > maxY) return 0;
        
        int count = 0;
        for (CellStyleAa cell : sortedCells) {
            if (cell.y == y) count++;
            else if (cell.y > y) break;
        }
        return count;
    }
    
    public CellStyleAa[] scanlineCells(int y) {
        if (y < minY || y > maxY) return new CellStyleAa[0];
        
        List<CellStyleAa> result = new ArrayList<>();
        for (CellStyleAa cell : sortedCells) {
            if (cell.y == y) {
                result.add(cell);
            } else if (cell.y > y) {
                break;
            }
        }
        
        return result.toArray(new CellStyleAa[0]);
    }
}
