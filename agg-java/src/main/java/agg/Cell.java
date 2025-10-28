package agg;

/**
 * Cell structure used in scanline rasterization.
 * Represents a single coverage cell with x position, coverage, and area values.
 */
public class Cell {
    public int x;
    public int cover;
    public int area;
    
    public Cell() {
        this.x = 0x7FFFFFFF;
        this.cover = 0;
        this.area = 0;
    }
    
    public Cell(int x, int cover, int area) {
        this.x = x;
        this.cover = cover;
        this.area = area;
    }
    
    public void set(int x, int cover, int area) {
        this.x = x;
        this.cover = cover;
        this.area = area;
    }
    
    public void set(Cell other) {
        this.x = other.x;
        this.cover = other.cover;
        this.area = other.area;
    }
    
    public void setCoord(int x) {
        this.x = x;
    }
    
    public void setCover(int cover, int area) {
        this.cover = cover;
        this.area = area;
    }
    
    public void addCover(int cover, int area) {
        this.cover += cover;
        this.area += area;
    }
}
