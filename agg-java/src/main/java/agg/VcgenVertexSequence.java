package agg;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertex generator that manages a sequence of vertices with optional path shortening.
 * Translates vcgen_vertex_sequence from C++ AGG library.
 */
public class VcgenVertexSequence {
    private static class VertexDistCmd {
        double x, y;
        int cmd;
        double dist;

        VertexDistCmd(double x, double y, int cmd) {
            this.x = x;
            this.y = y;
            this.cmd = cmd;
            this.dist = 0;
        }
    }

    private List<VertexDistCmd> srcVertices;
    private int flags;
    private int curVertex;
    private double shorten;
    private boolean ready;

    public VcgenVertexSequence() {
        srcVertices = new ArrayList<>();
        flags = 0;
        curVertex = 0;
        shorten = 0.0;
        ready = false;
    }

    public void removeAll() {
        ready = false;
        srcVertices.clear();
        curVertex = 0;
        flags = 0;
    }

    public void addVertex(double x, double y, int cmd) {
        ready = false;
        if (AggBasics.isMoveTo(cmd)) {
            if (!srcVertices.isEmpty()) {
                VertexDistCmd last = srcVertices.get(srcVertices.size() - 1);
                last.x = x;
                last.y = y;
                last.cmd = cmd;
            } else {
                srcVertices.add(new VertexDistCmd(x, y, cmd));
            }
        } else {
            if (AggBasics.isVertex(cmd)) {
                srcVertices.add(new VertexDistCmd(x, y, cmd));
            } else {
                flags = cmd & AggBasics.PATH_FLAGS_MASK;
            }
        }
    }

    public void rewind(int pathId) {
        if (!ready) {
            // Close path if needed
            boolean closed = (flags & AggBasics.PATH_FLAGS_CLOSE) != 0;
            if (closed && srcVertices.size() > 1) {
                VertexDistCmd first = srcVertices.get(0);
                VertexDistCmd last = srcVertices.get(srcVertices.size() - 1);
                if (first.x != last.x || first.y != last.y) {
                    srcVertices.add(new VertexDistCmd(first.x, first.y, AggBasics.PATH_CMD_LINE_TO));
                }
            }
            
            // Apply shortening if needed
            if (shorten > 0.0 && srcVertices.size() > 1) {
                shortenPathInternal(shorten);
            }
        }
        ready = true;
        curVertex = 0;
    }

    public int vertex(double[] xy) {
        if (!ready) {
            rewind(0);
        }

        if (curVertex == srcVertices.size()) {
            curVertex++;
            return AggBasics.PATH_CMD_END_POLY | flags;
        }

        if (curVertex > srcVertices.size()) {
            return AggBasics.PATH_CMD_STOP;
        }

        VertexDistCmd v = srcVertices.get(curVertex++);
        xy[0] = v.x;
        xy[1] = v.y;
        return v.cmd;
    }

    public void setShorten(double s) {
        shorten = s;
    }

    public double getShorten() {
        return shorten;
    }
    
    private void shortenPathInternal(double s) {
        if (s <= 0.0 || srcVertices.size() < 2) return;
        
        double d;
        int n = srcVertices.size() - 2;
        while (n >= 0 && s > 0) {
            VertexDistCmd v1 = srcVertices.get(n);
            VertexDistCmd v2 = srcVertices.get(n + 1);
            d = Math.sqrt((v2.x - v1.x) * (v2.x - v1.x) + (v2.y - v1.y) * (v2.y - v1.y));
            if (d > s) break;
            srcVertices.remove(srcVertices.size() - 1);
            s -= d;
            --n;
        }
        
        if (srcVertices.size() >= 2 && s > 0) {
            VertexDistCmd prev = srcVertices.get(srcVertices.size() - 2);
            VertexDistCmd last = srcVertices.get(srcVertices.size() - 1);
            d = Math.sqrt((last.x - prev.x) * (last.x - prev.x) + (last.y - prev.y) * (last.y - prev.y));
            if (d > 0) {
                double t = (d - s) / d;
                last.x = prev.x + (last.x - prev.x) * t;
                last.y = prev.y + (last.y - prev.y) * t;
            }
        }
    }
}
