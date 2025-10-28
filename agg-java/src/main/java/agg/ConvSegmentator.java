package agg;

/**
 * Converter that breaks paths into individual segments.
 * Useful for processing paths segment by segment.
 */
public class ConvSegmentator implements VertexSource {
    private VertexSource source;
    private double approximationScale;
    
    public ConvSegmentator(VertexSource source) {
        this.source = source;
        this.approximationScale = 1.0;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    public void approximationScale(double s) {
        this.approximationScale = s;
    }
    
    public double approximationScale() {
        return approximationScale;
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
