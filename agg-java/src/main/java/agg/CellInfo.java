package agg;

/**
 * Simplified cell information for rendering.
 * Contains only the essential geometric data needed for final scanline rendering.
 */
public class CellInfo {
    public int x;      // X coordinate
    public int area;   // Cell area
    public int cover;  // Cell coverage
    
    public CellInfo() {
        x = 0;
        area = 0;
        cover = 0;
    }
}
