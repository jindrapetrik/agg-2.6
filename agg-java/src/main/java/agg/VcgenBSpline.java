package agg;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertex generator that creates B-spline interpolation from a sequence of vertices.
 * Translates vcgen_bspline from C++ AGG library.
 */
public class VcgenBSpline {
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final int STATUS_INITIAL = 0;
    private static final int STATUS_READY = 1;
    private static final int STATUS_POLYGON = 2;
    private static final int STATUS_END_POLY = 3;
    private static final int STATUS_STOP = 4;

    private List<Point> srcVertices;
    private BSpline splineX;
    private BSpline splineY;
    private double interpolationStep;
    private int closed;
    private int status;
    private int srcVertex;
    private double curAbscissa;
    private double maxAbscissa;

    public VcgenBSpline() {
        srcVertices = new ArrayList<>();
        splineX = new BSpline();
        splineY = new BSpline();
        interpolationStep = 1.0 / 50.0;
        closed = 0;
        status = STATUS_INITIAL;
        srcVertex = 0;
        curAbscissa = 0.0;
        maxAbscissa = 0.0;
    }

    public void setInterpolationStep(double v) {
        interpolationStep = v;
    }

    public double getInterpolationStep() {
        return interpolationStep;
    }

    public void removeAll() {
        srcVertices.clear();
        closed = 0;
        status = STATUS_INITIAL;
        srcVertex = 0;
    }

    public void addVertex(double x, double y, int cmd) {
        status = STATUS_INITIAL;
        if (AggBasics.isMoveTo(cmd)) {
            srcVertices.add(new Point(x, y));
        } else {
            if (AggBasics.isVertex(cmd)) {
                srcVertices.add(new Point(x, y));
            } else {
                closed = AggBasics.getCloseFlag(cmd);
            }
        }
    }

    public void rewind(int pathId) {
        curAbscissa = 0.0;
        maxAbscissa = 0.0;
        srcVertex = 0;

        if (status == STATUS_INITIAL && srcVertices.size() > 2) {
            int n = srcVertices.size();
            if (closed != 0) {
                splineX.init(n + 3);
                splineY.init(n + 3);
                
                for (int i = 0; i < n; i++) {
                    Point p = srcVertices.get(i);
                    splineX.addPoint(i, p.x);
                    splineY.addPoint(i, p.y);
                }
                
                // Add wrapping points for closed spline
                splineX.addPoint(n, srcVertices.get(0).x);
                splineY.addPoint(n, srcVertices.get(0).y);
                splineX.addPoint(n + 1, srcVertices.get(1).x);
                splineY.addPoint(n + 1, srcVertices.get(1).y);
                splineX.addPoint(n + 2, srcVertices.get(2).x);
                splineY.addPoint(n + 2, srcVertices.get(2).y);
                
                maxAbscissa = n - 1;
            } else {
                splineX.init(n);
                splineY.init(n);
                
                for (int i = 0; i < n; i++) {
                    Point p = srcVertices.get(i);
                    splineX.addPoint(i, p.x);
                    splineY.addPoint(i, p.y);
                }
                
                maxAbscissa = n - 1;
            }
            
            splineX.prepare();
            splineY.prepare();
            status = STATUS_READY;
        }
    }

    public int vertex(double[] xy) {
        if (status == STATUS_INITIAL) {
            rewind(0);
        }

        switch (status) {
            case STATUS_READY:
                if (srcVertices.size() < 3) {
                    xy[0] = 0;
                    xy[1] = 0;
                    return AggBasics.PATH_CMD_STOP;
                }
                curAbscissa = 0.0;
                srcVertex = 0;
                status = STATUS_POLYGON;
                xy[0] = splineX.get(0);
                xy[1] = splineY.get(0);
                return AggBasics.PATH_CMD_MOVE_TO;

            case STATUS_POLYGON:
                curAbscissa += interpolationStep;
                if (curAbscissa > maxAbscissa) {
                    status = STATUS_END_POLY;
                    return vertex(xy);
                }
                xy[0] = splineX.get(curAbscissa);
                xy[1] = splineY.get(curAbscissa);
                return AggBasics.PATH_CMD_LINE_TO;

            case STATUS_END_POLY:
                status = STATUS_STOP;
                return AggBasics.PATH_CMD_END_POLY | closed;

            case STATUS_STOP:
                return AggBasics.PATH_CMD_STOP;
        }

        return AggBasics.PATH_CMD_STOP;
    }
}
