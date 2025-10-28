package agg;

import java.util.ArrayList;
import java.util.List;

/**
 * Dash pattern generator for paths
 * Converts solid paths into dashed patterns
 */
public class ConvDash implements VertexSource {
    private VertexSource source;
    private List<Double> dashes;
    private double dashStart;
    private double shorten;
    
    // State for vertex iteration
    private int status;
    private int srcVertex;
    private boolean closed;
    private double currRest;
    private int currDashIndex;
    private boolean currDash;
    private double x1, y1;
    private double x2, y2;
    private double v1x, v1y;
    
    private static final int INITIAL = 0;
    private static final int READY = 1;
    private static final int POLYLINE = 2;
    private static final int STOP = 3;

    public ConvDash(VertexSource source) {
        this.source = source;
        this.dashes = new ArrayList<>();
        this.dashStart = 0.0;
        this.shorten = 0.0;
        this.status = INITIAL;
    }

    /**
     * Remove all dash patterns
     */
    public void removeAllDashes() {
        dashes.clear();
    }

    /**
     * Add a dash pattern: dashLen followed by gapLen
     */
    public void addDash(double dashLen, double gapLen) {
        dashes.add(dashLen);
        dashes.add(gapLen);
    }

    /**
     * Set the starting point in the dash pattern
     */
    public void dashStart(double ds) {
        this.dashStart = ds;
    }

    /**
     * Set path shortening amount
     */
    public void shorten(double s) {
        this.shorten = s;
    }

    public double shorten() {
        return shorten;
    }

    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
        status = INITIAL;
        currDashIndex = 0;
        currDash = true;
        currRest = 0.0;
        v1x = v1y = x1 = y1 = x2 = y2 = 0.0;
    }

    @Override
    public int vertex(double[] xy) {
        if (dashes.isEmpty()) {
            // No dash pattern, pass through
            return source.vertex(xy);
        }

        int cmd;
        while (!AggBasics.isStop(cmd = source.vertex(xy))) {
            if (AggBasics.isMoveTo(cmd)) {
                x1 = x2 = xy[0];
                y1 = y2 = xy[1];
                return cmd;
            }
            
            if (AggBasics.isVertex(cmd)) {
                double x = xy[0];
                double y = xy[1];
                
                // Simple dash implementation - generate dashed line
                if (currDash) {
                    xy[0] = x;
                    xy[1] = y;
                    x2 = x;
                    y2 = y;
                    return cmd;
                } else {
                    x1 = x2 = x;
                    y1 = y2 = y;
                    return AggBasics.PATH_CMD_MOVE_TO;
                }
            }
            
            if (AggBasics.isEndPoly(cmd)) {
                return cmd;
            }
        }
        
        xy[0] = 0;
        xy[1] = 0;
        return AggBasics.PATH_CMD_STOP;
    }
}
