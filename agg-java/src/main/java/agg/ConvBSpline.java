package agg;

/**
 * Path converter that applies B-spline interpolation to paths.
 * Translates conv_bspline from C++ AGG library.
 */
public class ConvBSpline implements VertexSource {
    private VertexSource source;
    private VcgenBSpline generator;
    private int status;

    private static final int STATUS_INITIAL = 0;
    private static final int STATUS_ACCUMULATE = 1;
    private static final int STATUS_GENERATE = 2;

    public ConvBSpline(VertexSource source) {
        this.source = source;
        this.generator = new VcgenBSpline();
        this.status = STATUS_INITIAL;
    }

    public void attach(VertexSource source) {
        this.source = source;
    }

    public void setInterpolationStep(double v) {
        generator.setInterpolationStep(v);
    }

    public double getInterpolationStep() {
        return generator.getInterpolationStep();
    }

    @Override
    public void rewind(int pathId) {
        status = STATUS_INITIAL;
        source.rewind(pathId);
        generator.removeAll();
    }

    @Override
    public int vertex(double[] xy) {
        int cmd;
        while (true) {
            switch (status) {
                case STATUS_INITIAL:
                    generator.removeAll();
                    status = STATUS_ACCUMULATE;
                    // fall through

                case STATUS_ACCUMULATE:
                    cmd = source.vertex(xy);
                    if (AggBasics.isStop(cmd)) {
                        status = STATUS_GENERATE;
                        generator.rewind(0);
                        continue;
                    }
                    generator.addVertex(xy[0], xy[1], cmd);
                    if (AggBasics.isMoveTo(cmd)) {
                        status = STATUS_GENERATE;
                        generator.rewind(0);
                        cmd = generator.vertex(xy);
                        if (AggBasics.isStop(cmd)) {
                            status = STATUS_INITIAL;
                            continue;
                        }
                        return cmd;
                    }
                    return cmd;

                case STATUS_GENERATE:
                    cmd = generator.vertex(xy);
                    if (AggBasics.isStop(cmd)) {
                        status = STATUS_INITIAL;
                        continue;
                    }
                    return cmd;
            }
        }
    }
}
