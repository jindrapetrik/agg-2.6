package agg;

/**
 * Star shape vertex generator
 * Creates vertices for star shapes
 */
public class Star implements VertexSource {
    private double x, y;
    private double r1, r2;  // outer and inner radius
    private int numPoints;
    private int vertex;
    private double startAngle;
    
    public Star(double x, double y, double r1, double r2, int numPoints) {
        this.x = x;
        this.y = y;
        this.r1 = r1;
        this.r2 = r2;
        this.numPoints = numPoints;
        this.startAngle = 0;
        this.vertex = 0;
    }
    
    public Star(double x, double y, double r1, double r2, int numPoints, double startAngle) {
        this(x, y, r1, r2, numPoints);
        this.startAngle = startAngle;
    }
    
    @Override
    public void rewind(int pathId) {
        vertex = 0;
    }
    
    @Override
    public int vertex(double[] xy) {
        int totalVertices = numPoints * 2;
        
        if (vertex >= totalVertices) {
            xy[0] = 0;
            xy[1] = 0;
            return AggBasics.PATH_CMD_END_POLY | AggBasics.PATH_FLAGS_CLOSE;
        }
        
        double angle = startAngle + 2.0 * Math.PI * vertex / totalVertices;
        double r = (vertex % 2 == 0) ? r1 : r2;
        
        xy[0] = x + Math.cos(angle) * r;
        xy[1] = y + Math.sin(angle) * r;
        
        int cmd = (vertex == 0) ? AggBasics.PATH_CMD_MOVE_TO : AggBasics.PATH_CMD_LINE_TO;
        vertex++;
        return cmd;
    }
}
