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
     * Start a new path.
     * Returns the path ID which can be used to reference this path later.
     * 
     * @return path ID (index of current position)
     */
    public int startNewPath() {
        if (!commands.isEmpty()) {
            int lastCmd = commands.get(commands.size() - 1);
            if (!isStop(lastCmd)) {
                // Add stop command to end previous path
                vertices.add(0.0);
                vertices.add(0.0);
                commands.add(PATH_CMD_STOP);
            }
        }
        return commands.size();
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
    
    /**
     * Concatenate path from another vertex source.
     * The path is added as-is, preserving all commands.
     * 
     * @param vs vertex source
     * @param pathId path identifier
     */
    public void concatPath(VertexSource vs, int pathId) {
        vs.rewind(pathId);
        double[] xy = new double[2];
        int cmd;
        while (!isStop(cmd = vs.vertex(xy))) {
            vertices.add(xy[0]);
            vertices.add(xy[1]);
            commands.add(cmd);
        }
    }
    
    /**
     * Invert (reverse) a polygon starting at the given index.
     * This reverses the vertex order while preserving the path structure.
     * Translates from C++ AGG path_storage::invert_polygon.
     * 
     * @param start starting index
     */
    public void invertPolygon(int start) {
        // Skip all non-vertices at the beginning
        while (start < commands.size() && !isVertex(commands.get(start))) {
            start++;
        }
        
        // Skip all insignificant move_to
        while (start + 1 < commands.size() && 
               isMoveTo(commands.get(start)) &&
               isMoveTo(commands.get(start + 1))) {
            start++;
        }
        
        // Find the last vertex
        int end = start + 1;
        while (end < commands.size() && !isNextPoly(commands.get(end))) {
            end++;
        }
        
        invertPolygon(start, end);
    }
    
    /**
     * Invert polygon between start and end indices.
     * Direct translation of C++ AGG path_storage::invert_polygon(start, end).
     * 
     * Before inversion, removes duplicate closing vertex if present (where last vertex
     * equals first vertex), as explicit closing is common in Flash format but causes
     * issues when paths are inverted with auto_close disabled.
     * 
     * @param start starting index (inclusive)
     * @param end ending index (exclusive)
     */
    private void invertPolygon(int start, int end) {
        if (start >= end || start >= commands.size()) {
            return;
        }
        
        // Check if the polygon has an explicit closing vertex (last == first)
        // This is common in Flash format: "Line x y" back to starting point
        if (end > start + 1) {
            int firstVertIdx = start * 2;
            int lastVertIdx = (end - 1) * 2;
            
            if (firstVertIdx + 1 < vertices.size() && lastVertIdx + 1 < vertices.size()) {
                double firstX = vertices.get(firstVertIdx);
                double firstY = vertices.get(firstVertIdx + 1);
                double lastX = vertices.get(lastVertIdx);
                double lastY = vertices.get(lastVertIdx + 1);
                
                // If first and last vertices are the same, remove the duplicate
                if (Math.abs(firstX - lastX) < 1e-10 && Math.abs(firstY - lastY) < 1e-10) {
                    // Remove the last command and its vertices
                    commands.remove(end - 1);
                    vertices.remove(lastVertIdx + 1);  // Remove Y
                    vertices.remove(lastVertIdx);      // Remove X
                    end--;  // Adjust end index
                }
            }
        }
        
        int tmpCmd = commands.get(start);
        
        end--; // Make "end" inclusive
        
        // Shift all commands to one position
        for (int i = start; i < end && i < commands.size() - 1; i++) {
            commands.set(i, commands.get(i + 1));
        }
        
        // Assign starting command to the ending command
        if (end < commands.size()) {
            commands.set(end, tmpCmd);
        }
        
        // Reverse the polygon vertices
        while (end > start) {
            swapVertices(start, end);
            start++;
            end--;
        }
    }
    
    /**
     * Invert (reverse) vertices between start and end indices.
     * The first vertex (MOVE_TO) stays in place, and the remaining vertices are reversed.
     * 
     * @param start starting index (inclusive)
     * @param end ending index (exclusive)
     */
    
    /**
     * Swap two vertices at the given indices.
     * 
     * @param idx1 first vertex index
     * @param idx2 second vertex index
     */
    private void swapVertices(int idx1, int idx2) {
        if (idx1 >= commands.size() || idx2 >= commands.size()) {
            return;
        }
        
        int i1 = idx1 * 2;
        int i2 = idx2 * 2;
        
        double tmpX = vertices.get(i1);
        double tmpY = vertices.get(i1 + 1);
        
        vertices.set(i1, vertices.get(i2));
        vertices.set(i1 + 1, vertices.get(i2 + 1));
        
        vertices.set(i2, tmpX);
        vertices.set(i2 + 1, tmpY);
    }
    
    @Override
    public void rewind(int pathId) {
        iteratorIndex = pathId;
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
