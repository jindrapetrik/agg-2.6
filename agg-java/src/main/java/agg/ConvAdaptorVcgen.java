package agg;

/**
 * Generic converter adapter that wraps a vertex generator.
 * Base class for converters like ConvStroke, ConvDash, etc.
 */
public class ConvAdaptorVcgen implements VertexSource {
    private VertexSource source;
    private int status;
    private int sourceVertex;
    private int id;
    
    protected static final int INITIAL = 0;
    protected static final int ACCUMULATE = 1;
    protected static final int GENERATE = 2;
    
    public ConvAdaptorVcgen(VertexSource source) {
        this.source = source;
        this.status = INITIAL;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    @Override
    public void rewind(int pathId) {
        this.id = pathId;
        this.status = INITIAL;
        if (source != null) {
            source.rewind(pathId);
        }
    }
    
    @Override
    public int vertex(double[] xy) {
        if (source == null) {
            return AggBasics.PATH_CMD_STOP;
        }
        
        int cmd;
        boolean done = false;
        
        while (!done) {
            switch (status) {
                case INITIAL:
                    status = ACCUMULATE;
                    // Fall through
                    
                case ACCUMULATE:
                    cmd = source.vertex(xy);
                    if (AggBasics.isStop(cmd)) {
                        return AggBasics.PATH_CMD_STOP;
                    }
                    
                    if (AggBasics.isVertex(cmd)) {
                        // Accumulate vertices - subclass responsibility
                        return cmd;
                    }
                    
                    if (AggBasics.isMoveTo(cmd)) {
                        return cmd;
                    }
                    break;
                    
                case GENERATE:
                    // Generate output - subclass responsibility
                    return AggBasics.PATH_CMD_STOP;
                    
                default:
                    done = true;
                    break;
            }
        }
        
        return AggBasics.PATH_CMD_STOP;
    }
    
    protected VertexSource getSource() {
        return source;
    }
    
    protected void setStatus(int status) {
        this.status = status;
    }
    
    protected int getStatus() {
        return status;
    }
}
