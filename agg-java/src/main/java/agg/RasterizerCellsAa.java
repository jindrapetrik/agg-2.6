package agg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Rasterizer cells for anti-aliased rendering.
 * This is the core rasterization engine that converts vector paths to coverage cells.
 */
public class RasterizerCellsAa {
    private static final int CELL_BLOCK_SHIFT = 12;
    private static final int CELL_BLOCK_SIZE = 1 << CELL_BLOCK_SHIFT;
    private static final int CELL_BLOCK_MASK = CELL_BLOCK_SIZE - 1;
    private static final int CELL_BLOCK_POOL = 256;
    private static final int CELL_BLOCK_LIMIT = 1024;
    
    private List<Cell[]> cells;
    private List<CellAa> sortedCells;
    private Cell currentCell;
    private int minX, minY, maxX, maxY;
    private boolean sorted;
    private int curX, curY;
    private int coverAccum;
    private int areaAccum;
    
    public RasterizerCellsAa() {
        cells = new ArrayList<>();
        sortedCells = new ArrayList<>();
        currentCell = new Cell();
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
        currentCell.setCoord(0x7FFFFFFF);
        currentCell.setCover(0, 0);
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
        
        int x_from, x_to;
        int rem, mod, lift, delta, first, incr;
        long p;
        
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
        
        // Vertical line - we have to calculate start and end cells,
        // and then the common values of area and coverage for all cells
        incr = 1;
        if (dx == 0) {
            int ex = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
            int two_fx = (x1 - (ex << AggBasics.POLY_SUBPIXEL_SHIFT)) << 1;
            int area;
            
            first = AggBasics.POLY_SUBPIXEL_SCALE;
            if (dy < 0) {
                first = 0;
                incr = -1;
            }
            
            x_from = x1;
            
            // First cell
            delta = first - fy1;
            currentCell.cover += delta;
            currentCell.area += two_fx * delta;
            
            ey1 += incr;
            setCurrentCell(ex, ey1);
            
            delta = first + first - AggBasics.POLY_SUBPIXEL_SCALE;
            area = two_fx * delta;
            while (ey1 != ey2) {
                currentCell.cover = delta;
                currentCell.area = area;
                ey1 += incr;
                setCurrentCell(ex, ey1);
            }
            
            // Last cell
            delta = fy2 - AggBasics.POLY_SUBPIXEL_SCALE + first;
            currentCell.cover += delta;
            currentCell.area += two_fx * delta;
            return;
        }
        
        // Ok, we have to render several hlines
        p = (AggBasics.POLY_SUBPIXEL_SCALE - fy1) * dx;
        first = AggBasics.POLY_SUBPIXEL_SCALE;
        
        if (dy < 0) {
            p = fy1 * dx;
            first = 0;
            incr = -1;
            dy = -dy;
        }
        
        delta = (int)(p / dy);
        mod = (int)(p % dy);
        
        if (mod < 0) {
            delta--;
            mod += (int)dy;
        }
        
        x_from = x1 + delta;
        renderHLine(ey1, x1, fy1, x_from, first);
        
        ey1 += incr;
        setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
        
        if (ey1 != ey2) {
            p = AggBasics.POLY_SUBPIXEL_SCALE * dx;
            lift = (int)(p / dy);
            rem = (int)(p % dy);
            
            if (rem < 0) {
                lift--;
                rem += (int)dy;
            }
            mod -= (int)dy;
            
            while (ey1 != ey2) {
                delta = lift;
                mod += rem;
                if (mod >= 0) {
                    mod -= (int)dy;
                    delta++;
                }
                
                x_to = x_from + delta;
                renderHLine(ey1, x_from, AggBasics.POLY_SUBPIXEL_SCALE - first, x_to, first);
                x_from = x_to;
                
                ey1 += incr;
                setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
            }
        }
        renderHLine(ey1, x_from, AggBasics.POLY_SUBPIXEL_SCALE - first, x2, fy2);
    }
    
    private void setCurrentCell(int x, int y) {
        if (currentCell.x != x || curY != y) {
            addCurrentCell();
            currentCell.setCoord(x);
            currentCell.setCover(0, 0);
            curX = x;
            curY = y;
        }
    }
    
    private void addCurrentCell() {
        if ((currentCell.area | currentCell.cover) != 0) {
            CellAa cell = new CellAa(curX, curY, currentCell.cover, currentCell.area);
            sortedCells.add(cell);
            sorted = false;
        }
    }
    
    private void renderHLine(int ey, int x1, int y1, int x2, int y2) {
        int ex1 = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ex2 = x2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int fx1 = x1 & AggBasics.POLY_SUBPIXEL_MASK;
        int fx2 = x2 & AggBasics.POLY_SUBPIXEL_MASK;
        
        int delta, p, first;
        long dx;
        int incr, lift, mod, rem;
        
        // Trivial case - happens often
        if (y1 == y2) {
            setCurrentCell(ex2, ey);
            return;
        }
        
        // Everything is located in a single cell - that is easy!
        if (ex1 == ex2) {
            delta = y2 - y1;
            currentCell.cover += delta;
            currentCell.area += (fx1 + fx2) * delta;
            return;
        }
        
        // Ok, we'll have to render a run of adjacent cells on the same hline
        p = (AggBasics.POLY_SUBPIXEL_SCALE - fx1) * (y2 - y1);
        first = AggBasics.POLY_SUBPIXEL_SCALE;
        incr = 1;
        
        dx = (long)x2 - (long)x1;
        
        if (dx < 0) {
            p = fx1 * (y2 - y1);
            first = 0;
            incr = -1;
            dx = -dx;
        }
        
        delta = (int)(p / dx);
        mod = (int)(p % dx);
        
        if (mod < 0) {
            delta--;
            mod += (int)dx;
        }
        
        currentCell.cover += delta;
        currentCell.area += (fx1 + first) * delta;
        
        ex1 += incr;
        setCurrentCell(ex1, ey);
        y1 += delta;
        
        if (ex1 != ex2) {
            p = AggBasics.POLY_SUBPIXEL_SCALE * (y2 - y1 + delta);
            lift = (int)(p / dx);
            rem = (int)(p % dx);
            
            if (rem < 0) {
                lift--;
                rem += (int)dx;
            }
            
            mod -= (int)dx;
            
            while (ex1 != ex2) {
                delta = lift;
                mod += rem;
                if (mod >= 0) {
                    mod -= (int)dx;
                    delta++;
                }
                
                currentCell.cover += delta;
                currentCell.area += AggBasics.POLY_SUBPIXEL_SCALE * delta;
                y1 += delta;
                ex1 += incr;
                setCurrentCell(ex1, ey);
            }
        }
        delta = y2 - y1;
        currentCell.cover += delta;
        currentCell.area += (fx2 + AggBasics.POLY_SUBPIXEL_SCALE - first) * delta;
    }
    
    public void sortCells() {
        if (sorted) return;
        
        addCurrentCell();
        currentCell.setCoord(0x7FFFFFFF);
        currentCell.setCover(0, 0);
        
        if (sortedCells.size() == 0) return;
        
        Collections.sort(sortedCells, new Comparator<CellAa>() {
            @Override
            public int compare(CellAa a, CellAa b) {
                int yCompare = Integer.compare(a.y, b.y);
                if (yCompare != 0) return yCompare;
                return Integer.compare(a.x, b.x);
            }
        });
        
        sorted = true;
    }
    
    public int minX() { return minX; }
    public int minY() { return minY; }
    public int maxX() { return maxX; }
    public int maxY() { return maxY; }
    
    public List<CellAa> getSortedCells() {
        return sortedCells;
    }
}
