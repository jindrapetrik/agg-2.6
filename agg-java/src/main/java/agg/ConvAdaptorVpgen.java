package agg;

/**
 * Vertex processor generator adapter.
 * Processes vertices one by one through a vertex processor.
 */
public class ConvAdaptorVpgen implements VertexSource {
    private VertexSource source;
    private double[] lastVertex;
    private int status;
    
    private static final int INITIAL = 0;
    private static final int ACCUMULATE = 1;
    private static final int GENERATE = 2;
    
    public ConvAdaptorVpgen(VertexSource source) {
        this.source = source;
        this.lastVertex = new double[2];
        this.status = INITIAL;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    @Override
    public void rewind(int pathId) {
        if (source != null) {
            source.rewind(pathId);
        }
        status = INITIAL;
    }
    
    @Override
    public int vertex(double[] xy) {
        if (source == null) {
            return AggBasics.PATH_CMD_STOP;
        }
        
        int cmd = AggBasics.PATH_CMD_STOP;
        boolean done = false;
        
        while (!done) {
            switch (status) {
                case INITIAL:
                    cmd = source.vertex(xy);
                    if (!AggBasics.isVertex(cmd)) {
                        return cmd;
                    }
                    lastVertex[0] = xy[0];
                    lastVertex[1] = xy[1];
                    status = ACCUMULATE;
                    return cmd;
                    
                case ACCUMULATE:
                    cmd = source.vertex(xy);
                    if (!AggBasics.isVertex(cmd)) {
                        return cmd;
                    }
                    // Process vertex - subclass can override
                    lastVertex[0] = xy[0];
                    lastVertex[1] = xy[1];
                    return cmd;
                    
                default:
                    done = true;
                    break;
            }
        }
        
        return AggBasics.PATH_CMD_STOP;
    }
}
