package agg;

/**
 * Converter that removes polygon closing flags from paths.
 * This converter processes paths and removes the close flag from end_poly commands,
 * effectively "unclosing" all polygons in the path.
 */
public class ConvUnclosePolygon implements VertexSource {
    private VertexSource source;
    
    public ConvUnclosePolygon(VertexSource source) {
        this.source = source;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
    }
    
    @Override
    public int vertex(double[] xy) {
        int cmd = source.vertex(xy);
        if (AggBasics.isEndPoly(cmd)) {
            cmd &= ~AggBasics.PATH_FLAGS_CLOSE;
        }
        return cmd;
    }
}
