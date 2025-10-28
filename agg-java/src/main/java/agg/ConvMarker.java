package agg;

/**
 * Marker converter - adds markers at path vertices
 */
public class ConvMarker implements VertexSource {
    private VertexSource source;
    private VertexSource markerShape;
    private Transform2D transform;
    private double markerX, markerY;
    private int status;
    
    private static final int INITIAL = 0;
    private static final int MARKERS = 1;
    private static final int POLYGON = 2;
    private static final int STOP = 3;

    public ConvMarker(VertexSource source, VertexSource markerShape) {
        this.source = source;
        this.markerShape = markerShape;
        this.transform = new Transform2D();
        this.status = INITIAL;
    }

    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
        status = INITIAL;
    }

    @Override
    public int vertex(double[] xy) {
        // Simple implementation: pass through source vertices
        // In a complete implementation, this would insert marker shapes
        // at each vertex position
        return source.vertex(xy);
    }
}
