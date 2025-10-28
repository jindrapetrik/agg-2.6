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
// Bounding rectangle calculation utilities
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Utilities for calculating bounding rectangles from vertex sources.
 * Java translation of agg_bounding_rect.h
 */
public final class BoundingRect {
    
    // Private constructor to prevent instantiation
    private BoundingRect() {}
    
    /**
     * Calculate bounding rectangle for a single path from a vertex source.
     * 
     * @param vs vertex source
     * @param pathId path identifier
     * @param result array to receive bounds [x1, y1, x2, y2] (length >= 4)
     * @return true if valid bounds were calculated, false if no vertices
     */
    public static boolean boundingRectSingle(VertexSource vs, int pathId, double[] result) {
        double x, y;
        boolean first = true;
        
        result[0] = 1.0;
        result[1] = 1.0;
        result[2] = 0.0;
        result[3] = 0.0;
        
        vs.rewind(pathId);
        double[] xy = new double[2];
        int cmd;
        
        while (!isStop(cmd = vs.vertex(xy))) {
            if (isVertex(cmd)) {
                x = xy[0];
                y = xy[1];
                
                if (first) {
                    result[0] = x;
                    result[1] = y;
                    result[2] = x;
                    result[3] = y;
                    first = false;
                } else {
                    if (x < result[0]) result[0] = x;
                    if (y < result[1]) result[1] = y;
                    if (x > result[2]) result[2] = x;
                    if (y > result[3]) result[3] = y;
                }
            }
        }
        
        return result[0] <= result[2] && result[1] <= result[3];
    }
    
    /**
     * Calculate bounding rectangle for multiple paths from a vertex source.
     * 
     * @param vs vertex source
     * @param pathIds array of path identifiers
     * @param start starting index in pathIds array
     * @param count number of paths to process
     * @param result array to receive bounds [x1, y1, x2, y2] (length >= 4)
     * @return true if valid bounds were calculated, false if no vertices
     */
    public static boolean boundingRect(VertexSource vs, int[] pathIds, 
                                      int start, int count, double[] result) {
        double x, y;
        boolean first = true;
        
        result[0] = 1.0;
        result[1] = 1.0;
        result[2] = 0.0;
        result[3] = 0.0;
        
        double[] xy = new double[2];
        
        for (int i = 0; i < count; i++) {
            vs.rewind(pathIds[start + i]);
            int cmd;
            
            while (!isStop(cmd = vs.vertex(xy))) {
                if (isVertex(cmd)) {
                    x = xy[0];
                    y = xy[1];
                    
                    if (first) {
                        result[0] = x;
                        result[1] = y;
                        result[2] = x;
                        result[3] = y;
                        first = false;
                    } else {
                        if (x < result[0]) result[0] = x;
                        if (y < result[1]) result[1] = y;
                        if (x > result[2]) result[2] = x;
                        if (y > result[3]) result[3] = y;
                    }
                }
            }
        }
        
        return result[0] <= result[2] && result[1] <= result[3];
    }
    
    /**
     * Calculate bounding rectangle and store in RectD object.
     * 
     * @param vs vertex source
     * @param pathId path identifier
     * @return RectD containing bounds, or null if no valid bounds
     */
    public static RectD boundingRectSingle(VertexSource vs, int pathId) {
        double[] result = new double[4];
        if (boundingRectSingle(vs, pathId, result)) {
            return new RectD(result[0], result[1], result[2], result[3]);
        }
        return null;
    }
}
