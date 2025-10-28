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
// Vertex source interface
//
//----------------------------------------------------------------------------

package agg;

/**
 * Interface for vertex sources - objects that generate vertices for paths.
 * This is the fundamental interface for all path-generating classes in AGG.
 */
public interface VertexSource {
    
    /**
     * Rewind the vertex source to the beginning of a path.
     * 
     * @param pathId path identifier
     */
    void rewind(int pathId);
    
    /**
     * Get the next vertex from the path.
     * 
     * @param xy array to receive vertex coordinates (must have length >= 2)
     *           xy[0] will contain x coordinate
     *           xy[1] will contain y coordinate
     * @return path command (one of PATH_CMD_* from AggBasics)
     */
    int vertex(double[] xy);
}
