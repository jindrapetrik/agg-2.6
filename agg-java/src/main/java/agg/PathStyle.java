package agg;

/**
 * Path style information for compound shapes.
 * Stores fill and line style indices for a path.
 */
public class PathStyle {
    public int pathId;
    public int leftFill;
    public int rightFill;
    public int line;
    
    public PathStyle() {
        this.pathId = 0;
        this.leftFill = -1;
        this.rightFill = -1;
        this.line = -1;
    }
    
    public PathStyle(int pathId, int leftFill, int rightFill, int line) {
        this.pathId = pathId;
        this.leftFill = leftFill;
        this.rightFill = rightFill;
        this.line = line;
    }
}
