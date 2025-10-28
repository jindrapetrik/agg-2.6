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
        int dx = x2 - x1;
        
        if (dx >= 0) {
            if (dx == 0 && y1 == y2) return;
        } else {
            if (y1 == y2) return;
        }
        
        int dy = y2 - y1;
        int ex1 = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ex2 = x2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ey1 = y1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int ey2 = y2 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int fy1 = y1 & AggBasics.POLY_SUBPIXEL_MASK;
        int fy2 = y2 & AggBasics.POLY_SUBPIXEL_MASK;
        
        if (ex1 < minX) minX = ex1;
        if (ex2 < minX) minX = ex2;
        if (ey1 < minY) minY = ey1;
        if (ey2 < minY) minY = ey2;
        if (ex1 > maxX) maxX = ex1;
        if (ex2 > maxX) maxX = ex2;
        if (ey1 > maxY) maxY = ey1;
        if (ey2 > maxY) maxY = ey2;
        
        setCurrentCell(ex1, ey1);
        
        // Vertical line
        if (ex1 == ex2) {
            renderHLine(ey1, ey2, x1, fy1, fy2);
            return;
        }
        
        // Horizontal line
        if (ey1 == ey2) {
            renderHLine(ey1, ey1 + 1, x1, fy1, AggBasics.POLY_SUBPIXEL_SCALE);
            setCurrentCell(ex2, ey2);
            return;
        }
        
        // Diagonal line - simplified version
        int lift, delta, mod;
        int x_from, x_to;
        int p;
        int first = AggBasics.POLY_SUBPIXEL_SCALE;
        int incr = 1;
        int dy_scaled;
        
        if (dy < 0) {
            p = (first - fy1) * dx;
            delta = p / dy;
            mod = p % dy;
            
            if (mod < 0) {
                delta--;
                mod += dy;
            }
            
            x_from = x1 + delta;
            renderHLine(ey1, ey1 + 1, x1, fy1, first);
            ey1++;
            
            setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
            
            if (ey1 != ey2) {
                p = AggBasics.POLY_SUBPIXEL_SCALE * dx;
                lift = p / -dy;
                delta = lift;
                mod = p % -dy;
                
                while (ey1 != ey2) {
                    delta = lift;
                    mod -= dy;
                    if (mod >= 0) {
                        mod -= dy;
                        delta++;
                    }
                    
                    x_to = x_from + delta;
                    renderHLine(ey1, ey1 + 1, x_from, AggBasics.POLY_SUBPIXEL_SCALE, first);
                    x_from = x_to;
                    
                    ey1++;
                    setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
                }
            }
            renderHLine(ey1, ey1 + 1, x_from, AggBasics.POLY_SUBPIXEL_SCALE, fy2);
        } else {
            p = (AggBasics.POLY_SUBPIXEL_SCALE - fy1) * dx;
            delta = p / dy;
            mod = p % dy;
            
            if (mod < 0) {
                delta--;
                mod += dy;
            }
            
            x_from = x1 + delta;
            renderHLine(ey1, ey1 + 1, x1, fy1, AggBasics.POLY_SUBPIXEL_SCALE);
            ey1++;
            
            setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
            
            if (ey1 != ey2) {
                p = AggBasics.POLY_SUBPIXEL_SCALE * dx;
                lift = p / dy;
                delta = lift;
                mod = p % dy;
                
                while (ey1 != ey2) {
                    delta = lift;
                    mod += dy;
                    if (mod >= 0) {
                        mod -= dy;
                        delta++;
                    }
                    
                    x_to = x_from + delta;
                    renderHLine(ey1, ey1 + 1, x_from, 0, AggBasics.POLY_SUBPIXEL_SCALE);
                    x_from = x_to;
                    
                    ey1++;
                    setCurrentCell(x_from >> AggBasics.POLY_SUBPIXEL_SHIFT, ey1);
                }
            }
            renderHLine(ey1, ey1 + 1, x_from, 0, fy2);
        }
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
    
    private void renderHLine(int ey1, int ey2, int x1, int fy1, int fy2) {
        int ex1 = x1 >> AggBasics.POLY_SUBPIXEL_SHIFT;
        int fx1 = x1 & AggBasics.POLY_SUBPIXEL_MASK;
        
        int delta;
        
        // Same Y - single scanline
        if (fy1 == fy2) {
            setCurrentCell(ex1, ey1);
            return;
        }
        
        // All in same cell
        if (ey1 == ey2) {
            delta = fy2 - fy1;
            currentCell.cover += delta;
            currentCell.area += (fx1 + fx1) * delta;
            return;
        }
        
        // Multiple scanlines - simplified for now
        delta = fy2 - fy1;
        currentCell.cover += delta;
        currentCell.area += (fx1 + fx1) * delta;
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
