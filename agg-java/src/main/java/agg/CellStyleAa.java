package agg;

/**
 * A pixel cell with style information for compound rasterization.
 * Stores both geometric data (x, y, area, cover) and style indices (left, right).
 * 
 * Translated from C++ AGG cell_style_aa struct.
 */
public class CellStyleAa {
    public int x;
    public int y;
    public int cover;
    public int area;
    public short left;   // Left fill style index
    public short right;  // Right fill style index
    
    public static final int MAX_INT = Integer.MAX_VALUE;
    
    public void initial() {
        x = MAX_INT;
        y = MAX_INT;
        cover = 0;
        area = 0;
        left = -1;
        right = -1;
    }
    
    public void style(CellStyleAa c) {
        left = c.left;
        right = c.right;
    }
    
    public int notEqual(int ex, int ey, CellStyleAa c) {
        // Returns 0 if cells are equal (same position and styles), non-zero otherwise
        return ((ex - x) & 0xFFFFFFFFL) != 0 ? 1 :
               ((ey - y) & 0xFFFFFFFFL) != 0 ? 1 :
               ((left - c.left) & 0xFFFF) != 0 ? 1 :
               ((right - c.right) & 0xFFFF) != 0 ? 1 : 0;
    }
}
