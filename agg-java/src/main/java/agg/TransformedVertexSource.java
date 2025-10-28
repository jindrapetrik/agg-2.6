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
// Transformed vertex source - applies transformation to another vertex source
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Vertex source that applies a transformation to another vertex source.
 * Useful utility for transforming paths without modifying the original source.
 */
public class TransformedVertexSource implements VertexSource {
    
    private VertexSource source;
    private Transform2D transform;
    
    /**
     * Default constructor.
     */
    public TransformedVertexSource() {
        this(null, null);
    }
    
    /**
     * Constructor with source and transformation.
     * 
     * @param source vertex source to transform
     * @param transform transformation to apply
     */
    public TransformedVertexSource(VertexSource source, Transform2D transform) {
        this.source = source;
        this.transform = transform;
    }
    
    /**
     * Set the source vertex generator.
     * 
     * @param source vertex source
     */
    public void setSource(VertexSource source) {
        this.source = source;
    }
    
    /**
     * Get the source vertex generator.
     * 
     * @return vertex source
     */
    public VertexSource getSource() {
        return source;
    }
    
    /**
     * Set the transformation.
     * 
     * @param transform transformation
     */
    public void setTransform(Transform2D transform) {
        this.transform = transform;
    }
    
    /**
     * Get the transformation.
     * 
     * @return transformation
     */
    public Transform2D getTransform() {
        return transform;
    }
    
    @Override
    public void rewind(int pathId) {
        if (source != null) {
            source.rewind(pathId);
        }
    }
    
    @Override
    public int vertex(double[] xy) {
        if (source == null) {
            return PATH_CMD_STOP;
        }
        
        int cmd = source.vertex(xy);
        
        if (transform != null && isVertex(cmd)) {
            transform.transform(xy);
        }
        
        return cmd;
    }
}
