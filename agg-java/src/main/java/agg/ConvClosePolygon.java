package agg;

/**
 * Converter that automatically closes all polygons in a path.
 * Translates agg_conv_close_polygon.h from C++ AGG library.
 */
public class ConvClosePolygon implements VertexSource {
    private VertexSource source;
    private int[] cmd = new int[2];
    private double[] x = new double[2];
    private double[] y = new double[2];
    private int vertex;
    private boolean lineTo;

    public ConvClosePolygon(VertexSource source) {
        this.source = source;
    }

    public void attach(VertexSource source) {
        this.source = source;
    }

    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
        vertex = 2;
        lineTo = false;
    }

    @Override
    public int vertex(double[] xy) {
        int cmd = AggBasics.PATH_CMD_STOP;
        
        while (true) {
            if (vertex < 2) {
                xy[0] = x[vertex];
                xy[1] = y[vertex];
                cmd = this.cmd[vertex];
                vertex++;
                break;
            }

            cmd = source.vertex(xy);

            if (AggBasics.isEndPoly(cmd)) {
                cmd |= AggBasics.PATH_FLAGS_CLOSE;
                break;
            }

            if (AggBasics.isStop(cmd)) {
                if (lineTo) {
                    this.cmd[0] = AggBasics.PATH_CMD_END_POLY | AggBasics.PATH_FLAGS_CLOSE;
                    this.cmd[1] = AggBasics.PATH_CMD_STOP;
                    vertex = 0;
                    lineTo = false;
                    continue;
                }
                break;
            }

            if (AggBasics.isMoveTo(cmd)) {
                if (lineTo) {
                    x[0] = 0.0;
                    y[0] = 0.0;
                    this.cmd[0] = AggBasics.PATH_CMD_END_POLY | AggBasics.PATH_FLAGS_CLOSE;
                    x[1] = xy[0];
                    y[1] = xy[1];
                    this.cmd[1] = cmd;
                    vertex = 0;
                    lineTo = false;
                    continue;
                }
                break;
            }

            if (AggBasics.isVertex(cmd)) {
                lineTo = true;
                break;
            }
        }
        return cmd;
    }
}
