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
// Vertex with distance calculation
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggMath.*;

/**
 * Vertex structure with distance calculation capability.
 * Used in path processing to filter coinciding vertices.
 * Java translation from AGG vertex_dist struct.
 */
public class VertexDist {
    
    public double x;
    public double y;
    public double dist;
    
    /**
     * Default constructor.
     */
    public VertexDist() {
        this.x = 0.0;
        this.y = 0.0;
        this.dist = 0.0;
    }
    
    /**
     * Constructor with coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public VertexDist(double x, double y) {
        this.x = x;
        this.y = y;
        this.dist = 0.0;
    }
    
    /**
     * Calculate distance to another vertex and return true if distance > epsilon.
     * This is used to filter out coinciding vertices.
     * 
     * @param val other vertex
     * @return true if vertices are not coinciding (dist > epsilon)
     */
    public boolean calcDistance(VertexDist val) {
        this.dist = AggMath.calcDistance(x, y, val.x, val.y);
        return this.dist > VERTEX_DIST_EPSILON;
    }
    
    /**
     * Set coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return String.format("VertexDist(%.4f, %.4f, dist=%.4f)", x, y, dist);
    }
}
