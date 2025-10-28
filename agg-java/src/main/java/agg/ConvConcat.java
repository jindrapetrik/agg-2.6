package agg;

/**
 * Concatenates two vertex sources
 */
public class ConvConcat implements VertexSource {
    private VertexSource source1;
    private VertexSource source2;
    private int status;
    
    private static final int SOURCE1 = 0;
    private static final int SOURCE2 = 1;
    private static final int STOP = 2;

    public ConvConcat(VertexSource source1, VertexSource source2) {
        this.source1 = source1;
        this.source2 = source2;
        this.status = SOURCE1;
    }

    @Override
    public void rewind(int pathId) {
        source1.rewind(pathId);
        source2.rewind(pathId);
        status = SOURCE1;
    }

    @Override
    public int vertex(double[] xy) {
        if (status == SOURCE1) {
            int cmd = source1.vertex(xy);
            if (!AggBasics.isStop(cmd)) {
                return cmd;
            }
            status = SOURCE2;
        }
        
        if (status == SOURCE2) {
            return source2.vertex(xy);
        }
        
        xy[0] = 0;
        xy[1] = 0;
        return AggBasics.PATH_CMD_STOP;
    }
}
