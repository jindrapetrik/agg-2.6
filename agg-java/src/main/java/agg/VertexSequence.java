package agg;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertex sequence container for path processing
 */
public class VertexSequence {
    private List<VertexDist> vertices;
    private boolean closed;

    public VertexSequence() {
        this.vertices = new ArrayList<>();
        this.closed = false;
    }

    public void add(VertexDist v) {
        vertices.add(v);
    }

    public void add(double x, double y) {
        vertices.add(new VertexDist(x, y));
    }

    public VertexDist get(int index) {
        return vertices.get(index);
    }

    public void set(int index, VertexDist v) {
        vertices.set(index, v);
    }

    public int size() {
        return vertices.size();
    }

    public void removeLast() {
        if (!vertices.isEmpty()) {
            vertices.remove(vertices.size() - 1);
        }
    }

    public void removeAll() {
        vertices.clear();
    }

    public void close(boolean flag) {
        this.closed = flag;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Shorten the path by distance s from the end
     */
    public void shorten(double s) {
        if (s > 0.0 && vertices.size() > 1) {
            double d;
            int n = vertices.size() - 2;
            while (n >= 0) {
                d = vertices.get(n).dist;
                if (d > s) break;
                removeLast();
                s -= d;
                --n;
            }
            if (vertices.size() < 2) {
                removeAll();
            } else {
                n = vertices.size() - 1;
                VertexDist prev = vertices.get(n - 1);
                VertexDist last = vertices.get(n);
                d = (prev.dist - s) / prev.dist;
                double x = prev.x + (last.x - prev.x) * d;
                double y = prev.y + (last.y - prev.y) * d;
                last.x = x;
                last.y = y;
                if (Math.abs(last.x - prev.x) < 1e-10 && Math.abs(last.y - prev.y) < 1e-10) {
                    removeLast();
                }
            }
        }
    }
}
