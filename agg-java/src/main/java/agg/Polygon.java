package agg;

/**
 * Polygon vertex generator
 * Creates vertices for regular polygons
 */
public class Polygon implements VertexSource {
    private double x, y;
    private double radius;
    private int numPoints;
    private int step;
    private int vertex;
    
    public Polygon(double x, double y, double radius, int numPoints) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.numPoints = numPoints;
        this.step = -1;
        this.vertex = 0;
    }
    
    public Polygon(double x, double y, double radius, int numPoints, double startAngle) {
        this(x, y, radius, numPoints);
        this.step = (int)(startAngle / (2.0 * Math.PI / numPoints));
    }
    
    @Override
    public void rewind(int pathId) {
        vertex = 0;
        step = -1;
    }
    
    @Override
    public int vertex(double[] xy) {
        if (vertex >= numPoints) {
            xy[0] = 0;
            xy[1] = 0;
            return AggBasics.PATH_CMD_END_POLY | AggBasics.PATH_FLAGS_CLOSE;
        }
        
        double angle = 2.0 * Math.PI * vertex / numPoints;
        xy[0] = x + Math.cos(angle) * radius;
        xy[1] = y + Math.sin(angle) * radius;
        
        int cmd = (vertex == 0) ? AggBasics.PATH_CMD_MOVE_TO : AggBasics.PATH_CMD_LINE_TO;
        vertex++;
        return cmd;
    }
}
