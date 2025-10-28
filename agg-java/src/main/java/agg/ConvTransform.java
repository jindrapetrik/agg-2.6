package agg;

/**
 * Transformation converter that applies an affine transformation to vertices.
 * 
 * This is the standard AGG transformation adapter that wraps any vertex source
 * and applies a transformation to all vertices as they are retrieved.
 * 
 * Translated from agg_conv_transform.h
 */
public class ConvTransform implements VertexSource {
    private VertexSource source;
    private Transform2D transform;
    
    /**
     * Creates a transformation converter.
     *
     * @param source the vertex source to transform
     * @param transform the transformation to apply
     */
    public ConvTransform(VertexSource source, Transform2D transform) {
        this.source = source;
        this.transform = transform;
    }
    
    /**
     * Attaches a new vertex source.
     *
     * @param source the new vertex source
     */
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    /**
     * Sets the transformation.
     *
     * @param transform the new transformation
     */
    public void setTransform(Transform2D transform) {
        this.transform = transform;
    }
    
    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
    }
    
    @Override
    public int vertex(double[] xy) {
        int cmd = source.vertex(xy);
        if (AggBasics.isVertex(cmd)) {
            Point2D p = transform.transform(new Point2D(xy[0], xy[1]));
            xy[0] = p.x;
            xy[1] = p.y;
        }
        return cmd;
    }
}
