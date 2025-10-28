package agg;

/**
 * Curve converter class for processing Bezier curves in paths.
 * 
 * Path storage can contain Bezier curves defined by their control points.
 * This class converts curve3 (quadratic) and curve4 (cubic) commands into
 * sequences of line segments using adaptive subdivision.
 * 
 * Translated from agg_conv_curve.h
 */
public class ConvCurve implements VertexSource {
    private VertexSource source;
    private double lastX, lastY;
    private Curve3 curve3;
    private Curve4 curve4;
    private int currentCurveType; // 0 = none, 3 = curve3, 4 = curve4
    
    /**
     * Creates a curve converter for the given vertex source.
     *
     * @param source the vertex source to process
     */
    public ConvCurve(VertexSource source) {
        this.source = source;
        this.curve3 = new Curve3();
        this.curve4 = new Curve4();
        this.currentCurveType = 0;
    }
    
    /**
     * Sets the approximation scale for curve subdivision.
     *
     * @param scale the approximation scale
     */
    public void approximationScale(double scale) {
        curve3.approximationScale(scale);
        curve4.approximationScale(scale);
    }
    
    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
        lastX = 0.0;
        lastY = 0.0;
        currentCurveType = 0;
    }
    
    @Override
    public int vertex(double[] xy) {
        // If we're in the middle of processing a curve, continue with it
        if (currentCurveType == 3) {
            int cmd = curve3.vertex(xy);
            if (!AggBasics.isStop(cmd)) {
                lastX = xy[0];
                lastY = xy[1];
                return cmd;
            }
            currentCurveType = 0;
        } else if (currentCurveType == 4) {
            int cmd = curve4.vertex(xy);
            if (!AggBasics.isStop(cmd)) {
                lastX = xy[0];
                lastY = xy[1];
                return cmd;
            }
            currentCurveType = 0;
        }
        
        // Get next vertex from source
        int cmd = source.vertex(xy);
        
        if (AggBasics.isCurve3(cmd)) {
            // Quadratic Bezier curve - get control point and end point
            double cx = xy[0];
            double cy = xy[1];
            
            cmd = source.vertex(xy); // Get end point
            if (AggBasics.isVertex(cmd)) {
                double x2 = xy[0];
                double y2 = xy[1];
                
                curve3.init(lastX, lastY, cx, cy, x2, y2);
                curve3.rewind(0);
                currentCurveType = 3;
                
                // Get first vertex of the curve
                cmd = curve3.vertex(xy);
                lastX = xy[0];
                lastY = xy[1];
                return cmd;
            }
        } else if (AggBasics.isCurve4(cmd)) {
            // Cubic Bezier curve - get two control points and end point
            double cx1 = xy[0];
            double cy1 = xy[1];
            
            cmd = source.vertex(xy); // Get second control point
            if (AggBasics.isVertex(cmd)) {
                double cx2 = xy[0];
                double cy2 = xy[1];
                
                cmd = source.vertex(xy); // Get end point
                if (AggBasics.isVertex(cmd)) {
                    double x2 = xy[0];
                    double y2 = xy[1];
                    
                    curve4.init(lastX, lastY, cx1, cy1, cx2, cy2, x2, y2);
                    curve4.rewind(0);
                    currentCurveType = 4;
                    
                    // Get first vertex of the curve
                    cmd = curve4.vertex(xy);
                    lastX = xy[0];
                    lastY = xy[1];
                    return cmd;
                }
            }
        }
        
        if (AggBasics.isVertex(cmd)) {
            lastX = xy[0];
            lastY = xy[1];
        }
        
        return cmd;
    }
    
    /**
     * Attaches a new vertex source.
     *
     * @param source the new vertex source
     */
    public void attach(VertexSource source) {
        this.source = source;
    }
}
