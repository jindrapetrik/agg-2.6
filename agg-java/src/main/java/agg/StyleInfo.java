package agg;

/**
 * Information about cells belonging to a particular style.
 * Used internally by RasterizerCompoundAa to track which cells
 * belong to each fill style.
 */
public class StyleInfo {
    public int startCell;  // Index of first cell for this style
    public int numCells;   // Number of cells for this style
    public int lastX;      // Last X coordinate (for merging adjacent cells)
    
    public StyleInfo() {
        startCell = 0;
        numCells = 0;
        lastX = Integer.MIN_VALUE;
    }
}
