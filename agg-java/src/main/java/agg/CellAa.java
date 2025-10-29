package agg;

/**
 * Cell structure with Y coordinate for scanline rasterization.
 * Represents a single coverage cell with x, y position, coverage, and area values.
 */
public class CellAa {
    public int x;
    public int y;
    public int cover;
    public int area;
    
    public CellAa() {
        this.x = 0x7FFFFFFF;
        this.y = 0x7FFFFFFF;
        this.cover = 0;
        this.area = 0;
    }
    
    public CellAa(int x, int y, int cover, int area) {
        this.x = x;
        this.y = y;
        this.cover = cover;
        this.area = area;
    }
    
    public void set(int x, int y, int cover, int area) {
        this.x = x;
        this.y = y;
        this.cover = cover;
        this.area = area;
    }
    
    public void set(CellAa other) {
        this.x = other.x;
        this.y = other.y;
        this.cover = other.cover;
        this.area = other.area;
    }
}
