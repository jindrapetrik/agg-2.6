//----------------------------------------------------------------------------
// Anti-Grain Geometry - Version 2.6 (Java 8 port)
// Copyright (C) 2002-2005 Maxim Shemanarev (http://www.antigrain.com)
//
// Permission to copy, use, modify, sell and distribute this software 
// is granted provided this copyright notice appears in all copies. 
// This software is provided "as is" without express or implied
// warranty, and with no claim as to its suitability for any purpose.
//
//----------------------------------------------------------------------------
// Contact: mcseem@antigrain.com
//          mcseemagg@yahoo.com
//          http://www.antigrain.com
//----------------------------------------------------------------------------
//
// Path storage - container for storing path vertices
//
//----------------------------------------------------------------------------

package agg;

import java.util.ArrayList;
import java.util.List;

import static agg.AggBasics.*;

/**
 * Path storage container for storing and manipulating path vertices.
 * Simplified Java translation of agg_path_storage.h
 */
public class PathStorage implements VertexSource {
    
    private List<Double> vertices;
    private List<Integer> commands;
    private int iteratorIndex;
    
    /**
     * Default constructor.
     */
    public PathStorage() {
        vertices = new ArrayList<>();
        commands = new ArrayList<>();
        iteratorIndex = 0;
    }
    
    /**
     * Remove all vertices.
     */
    public void removeAll() {
        vertices.clear();
        commands.clear();
        iteratorIndex = 0;
    }
    
    /**
     * Get total number of vertices.
     * 
     * @return vertex count
     */
    public int totalVertices() {
        return commands.size();
    }
    
    /**
     * Get last vertex coordinates.
     * 
     * @param xy array to receive coordinates [x, y]
     * @return true if vertex exists
     */
    public boolean lastVertex(double[] xy) {
        if (commands.isEmpty()) {
            return false;
        }
        int idx = (commands.size() - 1) * 2;
        xy[0] = vertices.get(idx);
        xy[1] = vertices.get(idx + 1);
        return true;
    }
    
    /**
     * Get previous vertex coordinates.
     * 
     * @param xy array to receive coordinates [x, y]
     * @return true if vertex exists
     */
    public boolean prevVertex(double[] xy) {
        if (commands.size() < 2) {
            return false;
        }
        int idx = (commands.size() - 2) * 2;
        xy[0] = vertices.get(idx);
        xy[1] = vertices.get(idx + 1);
        return true;
    }
    
    /**
     * Get last x coordinate.
     * 
     * @return x coordinate or 0.0 if no vertices
     */
    public double lastX() {
        return commands.isEmpty() ? 0.0 : vertices.get((commands.size() - 1) * 2);
    }
    
    /**
     * Get last y coordinate.
     * 
     * @return y coordinate or 0.0 if no vertices
     */
    public double lastY() {
        return commands.isEmpty() ? 0.0 : vertices.get((commands.size() - 1) * 2 + 1);
    }
    
    /**
     * Move to a point (start new sub-path).
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void moveTo(double x, double y) {
        vertices.add(x);
        vertices.add(y);
        commands.add(PATH_CMD_MOVE_TO);
    }
    
    /**
     * Add line to a point.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void lineTo(double x, double y) {
        vertices.add(x);
        vertices.add(y);
        commands.add(PATH_CMD_LINE_TO);
    }
    
    /**
     * Add cubic Bezier curve.
     * 
     * @param x_ctrl1 first control point x
     * @param y_ctrl1 first control point y
     * @param x_ctrl2 second control point x
     * @param y_ctrl2 second control point y
     * @param x_to end point x
     * @param y_to end point y
     */
    public void curve3(double x_ctrl, double y_ctrl, double x_to, double y_to) {
        vertices.add(x_ctrl);
        vertices.add(y_ctrl);
        commands.add(PATH_CMD_CURVE3);
        vertices.add(x_to);
        vertices.add(y_to);
        commands.add(PATH_CMD_CURVE3);
    }
    
    /**
     * Add cubic Bezier curve (4 points).
     * 
     * @param x_ctrl1 first control point x
     * @param y_ctrl1 first control point y
     * @param x_ctrl2 second control point x
     * @param y_ctrl2 second control point y
     * @param x_to end point x
     * @param y_to end point y
     */
    public void curve4(double x_ctrl1, double y_ctrl1, 
                      double x_ctrl2, double y_ctrl2,
                      double x_to, double y_to) {
        vertices.add(x_ctrl1);
        vertices.add(y_ctrl1);
        commands.add(PATH_CMD_CURVE4);
        vertices.add(x_ctrl2);
        vertices.add(y_ctrl2);
        commands.add(PATH_CMD_CURVE4);
        vertices.add(x_to);
        vertices.add(y_to);
        commands.add(PATH_CMD_CURVE4);
    }
    
    /**
     * Close current polygon.
     */
    public void closePolygon() {
        if (!commands.isEmpty()) {
            commands.set(commands.size() - 1, 
                commands.get(commands.size() - 1) | PATH_FLAGS_CLOSE);
        }
    }
    
    /**
     * End current polygon (same as closePolygon).
     */
    public void endPoly() {
        closePolygon();
    }
    
    /**
     * Add arc.
     * 
     * @param x center x
     * @param y center y
     * @param rx radius x
     * @param ry radius y
     * @param start start angle
     * @param sweep sweep angle
     */
    public void arc(double x, double y, double rx, double ry, 
                   double start, double sweep) {
        Arc arc = new Arc(x, y, rx, ry, start, start + sweep, sweep > 0);
        arc.rewind(0);
        double[] xy = new double[2];
        int cmd;
        boolean first = true;
        while (!isStop(cmd = arc.vertex(xy))) {
            if (isVertex(cmd)) {
                if (first) {
                    if (commands.isEmpty() || isStop(commands.get(commands.size() - 1))) {
                        moveTo(xy[0], xy[1]);
                    } else {
                        lineTo(xy[0], xy[1]);
                    }
                    first = false;
                } else {
                    lineTo(xy[0], xy[1]);
                }
            }
        }
    }
    
    /**
     * Add vertices from another vertex source.
     * 
     * @param vs vertex source
     * @param pathId path identifier
     */
    public void addPath(VertexSource vs, int pathId) {
        vs.rewind(pathId);
        double[] xy = new double[2];
        int cmd;
        while (!isStop(cmd = vs.vertex(xy))) {
            if (isMoveTo(cmd)) {
                moveTo(xy[0], xy[1]);
            } else if (isLineTo(cmd)) {
                lineTo(xy[0], xy[1]);
            } else if (isVertex(cmd)) {
                lineTo(xy[0], xy[1]);
            }
            
            if (isClose(cmd)) {
                closePolygon();
            }
        }
    }
    
    @Override
    public void rewind(int pathId) {
        iteratorIndex = 0;
    }
    
    @Override
    public int vertex(double[] xy) {
        if (iteratorIndex >= commands.size()) {
            return PATH_CMD_STOP;
        }
        
        int idx = iteratorIndex * 2;
        xy[0] = vertices.get(idx);
        xy[1] = vertices.get(idx + 1);
        int cmd = commands.get(iteratorIndex);
        iteratorIndex++;
        
        return cmd;
    }
    
    /**
     * Transform all vertices in the path.
     * 
     * @param transform transformation to apply
     */
    public void transform(Transform2D transform) {
        for (int i = 0; i < vertices.size(); i += 2) {
            double x = vertices.get(i);
            double y = vertices.get(i + 1);
            double[] xy = new double[] {x, y};
            transform.transform(xy);
            vertices.set(i, xy[0]);
            vertices.set(i + 1, xy[1]);
        }
    }
}
