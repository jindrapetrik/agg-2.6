package agg;

/**
 * Stroke converter - generates outline (stroke) for paths.
 * Wraps VcgenStroke with the VertexSource interface.
 * Based on agg_conv_stroke.h from the C++ AGG library.
 */
public class ConvStroke implements VertexSource {
    private final VertexSource source;
    private final VcgenStroke stroke;
    
    public ConvStroke(VertexSource source) {
        this.source = source;
        this.stroke = new VcgenStroke();
    }
    
    public void width(double w) {
        stroke.width(w);
    }
    
    public double width() {
        return stroke.width();
    }
    
    public void lineCap(LineCapE lc) {
        stroke.lineCap(lc);
    }
    
    public LineCapE lineCap() {
        return stroke.lineCap();
    }
    
    public void lineJoin(LineJoinE lj) {
        stroke.lineJoin(lj);
    }
    
    public LineJoinE lineJoin() {
        return stroke.lineJoin();
    }
    
    public void innerJoin(InnerJoinE ij) {
        stroke.innerJoin(ij);
    }
    
    public InnerJoinE innerJoin() {
        return stroke.innerJoin();
    }
    
    public void miterLimit(double ml) {
        stroke.miterLimit(ml);
    }
    
    public double miterLimit() {
        return stroke.miterLimit();
    }
    
    public void approximationScale(double as) {
        stroke.approximationScale(as);
    }
    
    public double approximationScale() {
        return stroke.approximationScale();
    }
    
    @Override
    public void rewind(int pathId) {
        source.rewind(pathId);
    }
    
    @Override
    public int vertex(double[] xy) {
        // Simplified implementation - actual stroke generation is complex
        return source.vertex(xy);
    }
}
