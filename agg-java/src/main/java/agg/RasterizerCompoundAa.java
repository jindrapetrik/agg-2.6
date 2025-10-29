package agg;

import java.util.ArrayList;
import java.util.Arrays;

import static agg.AggBasics.*;
import agg.AggBasics.FillingRule;

/**
 * Compound rasterizer that tracks left AND right fills for each cell.
 * Enables perfect stitching of adjacent polygons by tracking fill styles
 * at the cell level and blending overlapping styles during rendering.
 * 
 * Translated from C++ AGG agg_rasterizer_compound_aa.h
 */
public class RasterizerCompoundAa {
    
    // Layer ordering modes
    public enum LayerOrder {
        LAYER_UNSORTED,
        LAYER_DIRECT,
        LAYER_INVERSE
    }
    
    // Anti-aliasing constants
    public static final int AA_SHIFT = 8;
    public static final int AA_SCALE = 1 << AA_SHIFT;
    public static final int AA_MASK = AA_SCALE - 1;
    public static final int AA_SCALE2 = AA_SCALE * 2;
    public static final int AA_MASK2 = AA_SCALE2 - 1;
    
    private RasterizerCellsCompound outline;
    private FillingRule fillingRule;
    private LayerOrder layerOrder;
    
    // Active Styles management
    private ArrayList<StyleInfo> styles;      // Active Styles (indexed by AST entry)
    private ArrayList<Integer> ast;            // Active Style Table (unique style IDs)
    private byte[] asm;                        // Active Style Mask (bit array)
    private ArrayList<CellInfo> cells;         // Cell data for current scanline
    private ArrayList<Integer> coverBuf;       // Coverage buffer
    
    private int minStyle;
    private int maxStyle;
    private int startX;
    private int startY;
    private int scanY;
    private int slStart;
    private int slLen;
    
    public RasterizerCompoundAa() {
        outline = new RasterizerCellsCompound();
        fillingRule = FillingRule.FILL_NON_ZERO;
        layerOrder = LayerOrder.LAYER_DIRECT;
        styles = new ArrayList<>();
        ast = new ArrayList<>();
        asm = new byte[0];
        cells = new ArrayList<>();
        coverBuf = new ArrayList<>();
        minStyle = Integer.MAX_VALUE;
        maxStyle = Integer.MIN_VALUE;
        startX = 0;
        startY = 0;
        scanY = Integer.MAX_VALUE;
        slStart = 0;
        slLen = 0;
    }
    
    public void reset() {
        outline.reset();
        minStyle = Integer.MAX_VALUE;
        maxStyle = Integer.MIN_VALUE;
        scanY = Integer.MAX_VALUE;
        slStart = 0;
        slLen = 0;
    }
    
    public void resetClipping() {
        outline.resetClipping();
    }
    
    public void clipBox(double x1, double y1, double x2, double y2) {
        outline.clipBox(x1, y1, x2, y2);
    }
    
    public void fillingRule(FillingRule rule) {
        fillingRule = rule;
    }
    
    public void layerOrder(LayerOrder order) {
        layerOrder = order;
    }
    
    public void styles(int left, int right) {
        CellStyleAa cell = outline.style();
        cell.left = (short) left;
        cell.right = (short) right;
        
        if (left >= 0 && left < minStyle) minStyle = left;
        if (left >= 0 && left > maxStyle) maxStyle = left;
        if (right >= 0 && right < minStyle) minStyle = right;
        if (right >= 0 && right > maxStyle) maxStyle = right;
    }
    
    public void moveTo(int x, int y) {
        outline.moveTo(x, y);
        startX = x;
        startY = y;
    }
    
    public void lineTo(int x, int y) {
        outline.lineTo(x, y);
    }
    
    /**
     * Convert double coordinate to integer coordinate with subpixel precision.
     * Equivalent to C++ conv_type::upscale()
     */
    private static int coordInt(double v) {
        return iround(v * POLY_SUBPIXEL_SCALE);
    }
    
    public void moveToD(double x, double y) {
        moveTo(coordInt(x), coordInt(y));
    }
    
    public void lineToD(double x, double y) {
        lineTo(coordInt(x), coordInt(y));
    }
    
    public void addVertex(double x, double y, int cmd) {
        if (isMoveTo(cmd)) {
            moveToD(x, y);
        } else if (isVertex(cmd)) {
            lineToD(x, y);
        } else if (isClose(cmd)) {
            outline.lineTo(startX, startY);
        }
    }
    
    public void edge(int x1, int y1, int x2, int y2) {
        if (outline.sorted()) reset();
        moveTo(x1, y1);
        lineTo(x2, y2);
    }
    
    public void edgeD(double x1, double y1, double x2, double y2) {
        edge(coordInt(x1), coordInt(y1), coordInt(x2), coordInt(y2));
    }
    
    public <VS extends VertexSource> void addPath(VS vs, int pathId) {
        double[] xy = new double[2];
        
        vs.rewind(pathId);
        if (outline.sorted()) reset();
        
        int cmd;
        while (!isStop(cmd = vs.vertex(xy))) {
            addVertex(xy[0], xy[1], cmd);
        }
    }
    
    // Accessors
    public int minX() { return outline.minX(); }
    public int minY() { return outline.minY(); }
    public int maxX() { return outline.maxX(); }
    public int maxY() { return outline.maxY(); }
    public int minStyle() { return minStyle; }
    public int maxStyle() { return maxStyle; }
    
    public void sort() {
        outline.sortCells();
    }
    
    public boolean rewindScanlines() {
        outline.sortCells();
        if (outline.totalCells() == 0) {
            return false;
        }
        scanY = outline.minY();
        styles.clear();
        return true;
    }
    
    public boolean navigateScanline(int y) {
        outline.sortCells();
        if (outline.totalCells() == 0 || y < outline.minY() || y > outline.maxY()) {
            return false;
        }
        scanY = y;
        styles.clear();
        return true;
    }
    
    public boolean hitTest(int tx, int ty) {
        if (!navigateScanline(ty)) {
            return false;
        }
        
        int numStyles = sweepStyles();
        if (numStyles <= 0) {
            return false;
        }
        
        ScanlineHitTest sl = new ScanlineHitTest(tx);
        for (int i = 0; i < numStyles; i++) {
            sweepScanline(sl, i);
            if (sl.hit()) {
                return true;
            }
        }
        return false;
    }
    
    private void addStyle(int styleId) {
        if (styleId < 0) return;
        
        // Ensure ASM array is large enough
        int byteSize = (styleId >> 3) + 1;
        if (asm.length < byteSize) {
            byte[] newAsm = new byte[byteSize];
            System.arraycopy(asm, 0, newAsm, 0, asm.length);
            asm = newAsm;
        }
        
        // Check if style already in AST using bit mask
        int bitIndex = styleId & 7;
        int byteIndex = styleId >> 3;
        
        if ((asm[byteIndex] & (1 << bitIndex)) == 0) {
            // New style - add to AST
            ast.add(styleId);
            asm[byteIndex] |= (1 << bitIndex);
        }
    }
    
    public int sweepStyles() {
        for (;;) {
            if (scanY > outline.maxY()) {
                return 0;
            }
            
            int numCells = outline.scanlineNumCells(scanY);
            CellStyleAa[] cellsArray = outline.scanlineCells(scanY);
            int numStyles = maxStyle - minStyle + 2;
            
            // Allocate memory for cells (each cell can have two styles)
            cells.clear();
            cells.ensureCapacity(numCells * 2);
            
            // Ensure AST has enough capacity
            ast.clear();
            ast.ensureCapacity(numStyles);
            
            // Ensure ASM array is large enough
            int byteSize = (numStyles + 7) >> 3;
            if (asm.length < byteSize) {
                asm = new byte[byteSize];
            }
            Arrays.fill(asm, (byte) 0);
            
            // Allocate styles array
            styles.clear();
            while (styles.size() < numStyles) {
                styles.add(new StyleInfo());
            }
            
            if (numCells > 0) {
                // Pre-add zero (for no-fill style, that is, -1).
                // We need that to ensure that the "-1 style" would go first.
                asm[0] |= 1;
                ast.add(0);
                StyleInfo style = styles.get(0);
                style.startCell = 0;
                style.numCells = 0;
                style.lastX = Integer.MIN_VALUE;
                
                slStart = cellsArray[0].x;
                slLen = cellsArray[numCells - 1].x - slStart + 1;
                
                // First pass: collect unique styles and count cells per style
                for (int i = 0; i < numCells; i++) {
                    CellStyleAa currCell = cellsArray[i];
                    addStyle(currCell.left);
                    addStyle(currCell.right);
                }
                
                // Convert the Y-histogram into the array of starting indexes
                int startCell = 0;
                for (int i = 0; i < ast.size(); i++) {
                    StyleInfo st = styles.get(ast.get(i));
                    int v = st.startCell;
                    st.startCell = startCell;
                    startCell += v;
                }
                
                // Pre-allocate cells array
                for (int i = 0; i < numCells * 2; i++) {
                    cells.add(new CellInfo());
                }
                
                // Second pass: populate cells array, processing left and right separately
                for (int i = 0; i < numCells; i++) {
                    CellStyleAa currCell = cellsArray[i];
                    
                    // Process left style
                    int styleId = (currCell.left < 0) ? 0 : currCell.left - minStyle + 1;
                    StyleInfo st = styles.get(styleId);
                    
                    if (currCell.x == st.lastX) {
                        // Same x as previous cell for this style - accumulate
                        CellInfo cell = cells.get(st.startCell + st.numCells - 1);
                        cell.area += currCell.area;
                        cell.cover += currCell.cover;
                    } else {
                        // New x position - create new cell
                        CellInfo cell = cells.get(st.startCell + st.numCells);
                        cell.x = currCell.x;
                        cell.area = currCell.area;
                        cell.cover = currCell.cover;
                        st.lastX = currCell.x;
                        st.numCells++;
                    }
                    
                    // Process right style (with negated area/cover)
                    styleId = (currCell.right < 0) ? 0 : currCell.right - minStyle + 1;
                    st = styles.get(styleId);
                    
                    if (currCell.x == st.lastX) {
                        // Same x as previous cell for this style - accumulate (negated)
                        CellInfo cell = cells.get(st.startCell + st.numCells - 1);
                        cell.area -= currCell.area;
                        cell.cover -= currCell.cover;
                    } else {
                        // New x position - create new cell (negated)
                        CellInfo cell = cells.get(st.startCell + st.numCells);
                        cell.x = currCell.x;
                        cell.area = -currCell.area;
                        cell.cover = -currCell.cover;
                        st.lastX = currCell.x;
                        st.numCells++;
                    }
                }
            }
            
            if (ast.size() > 1) break;
            scanY++;
        }
        
        scanY++;
        
        // Sort styles if needed (excluding first element which is style 0 for -1)
        if (layerOrder != LayerOrder.LAYER_UNSORTED && ast.size() > 2) {
            // Sort elements from index 1 to end
            ArrayList<Integer> toSort = new ArrayList<>(ast.subList(1, ast.size()));
            if (layerOrder == LayerOrder.LAYER_DIRECT) {
                toSort.sort((a, b) -> b - a);  // unsigned_greater
            } else {
                toSort.sort((a, b) -> a - b);  // unsigned_less
            }
            // Replace sorted portion
            for (int i = 0; i < toSort.size(); i++) {
                ast.set(i + 1, toSort.get(i));
            }
        }
        
        return ast.size() - 1;
    }
    
    public int scanlineStart() {
        return slStart;
    }
    
    public int scanlineLength() {
        return slLen;
    }
    
    public int style(int styleIdx) {
        if (styleIdx < 0 || styleIdx >= ast.size()) {
            return -1;
        }
        return ast.get(styleIdx);
    }
    
    public int[] allocateCoverBuffer(int len) {
        if (coverBuf.size() < len) {
            coverBuf.clear();
            for (int i = 0; i < len; i++) {
                coverBuf.add(0);
            }
        }
        
        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = i < coverBuf.size() ? coverBuf.get(i) : 0;
        }
        return result;
    }
    
    public int calculateAlpha(int area) {
        int cover = area >> (POLY_SUBPIXEL_SHIFT * 2 + 1 - AA_SHIFT);
        if (cover < 0) cover = -cover;
        
        if (fillingRule == FillingRule.FILL_EVEN_ODD) {
            cover &= AA_MASK2;
            if (cover > AA_SCALE) {
                cover = AA_SCALE2 - cover;
            }
        }
        
        if (cover > AA_MASK) cover = AA_MASK;
        return cover;
    }
    
    public <SL extends Scanline> boolean sweepScanline(SL sl, int styleIdx) {
        int scanY = this.scanY - 1;
        if (scanY > outline.maxY()) {
            return false;
        }
        
        sl.resetSpans();
        
        // styleIdx is the index into the styles array (0-based)
        if (styleIdx < 0 || styleIdx >= styles.size()) {
            return false;
        }
        
        StyleInfo st = styles.get(styleIdx);
        int numCells = st.numCells;
        int cellIdx = st.startCell;
        
        int cover = 0;
        for (int i = 0; i < numCells; i++) {
            CellInfo cell = cells.get(cellIdx + i);
            int alpha;
            int x = cell.x;
            int area = cell.area;
            
            cover += cell.cover;
            
            if (area != 0) {
                alpha = calculateAlpha((cover << (POLY_SUBPIXEL_SHIFT + 1)) - area);
                sl.addCell(x, alpha);
                x++;
            }
            
            if (i + 1 < numCells) {
                CellInfo nextCell = cells.get(cellIdx + i + 1);
                if (nextCell.x > x) {
                    alpha = calculateAlpha(cover << (POLY_SUBPIXEL_SHIFT + 1));
                    if (alpha != 0) {
                        sl.addSpan(x, nextCell.x - x, alpha);
                    }
                }
            }
        }
        
        if (sl.numSpans() == 0) {
            return false;
        }
        
        sl.finalize(scanY);
        return true;
    }
    
    // Helper class for hit testing
    private static class ScanlineHitTest implements Scanline {
        private int hitX;
        private boolean isHit;
        
        public ScanlineHitTest(int x) {
            hitX = x;
            isHit = false;
        }
        
        public boolean hit() {
            return isHit;
        }
        
        @Override
        public void resetSpans() {
            isHit = false;
        }
        
        @Override
        public void addCell(int x, int cover) {
            if (x == hitX) {
                isHit = true;
            }
        }
        
        @Override
        public void addSpan(int x, int len, int cover) {
            if (hitX >= x && hitX < x + len) {
                isHit = true;
            }
        }
        
        @Override
        public void finalize(int y) {
        }
        
        @Override
        public int numSpans() {
            return isHit ? 1 : 0;
        }
        
        @Override
        public int y() {
            return 0;
        }
    }
}
