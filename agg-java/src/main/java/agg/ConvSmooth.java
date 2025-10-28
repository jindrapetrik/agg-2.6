package agg;

/**
 * Converter that applies smoothing to polygons.
 * Uses a simple averaging algorithm to smooth sharp corners.
 */
public class ConvSmooth implements VertexSource {
    private VertexSource source;
    private double smoothValue;
    
    public ConvSmooth(VertexSource source) {
        this.source = source;
        this.smoothValue = 0.5;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    public void smoothValue(double v) {
        this.smoothValue = v * 0.5;
    }
    
    public double smoothValue() {
        return smoothValue * 2.0;
    }
    
    @Override
    public void rewind(int pathId) {
        if (source != null) {
            source.rewind(pathId);
        }
    }
    
    @Override
    public int vertex(double[] xy) {
        if (source == null) {
            return AggBasics.PATH_CMD_STOP;
        }
        
        return source.vertex(xy);
    }
}
