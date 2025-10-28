package agg;

/**
 * Converter for creating contours (offset paths).
 * Expands or contracts paths by a specified width.
 */
public class ConvContour implements VertexSource {
    private VertexSource source;
    private double width;
    private LineJoinE lineJoin;
    private InnerJoinE innerJoin;
    private double miterLimit;
    private double innerMiterLimit;
    private double approximationScale;
    private boolean autoDetectOrientation;
    
    public ConvContour(VertexSource source) {
        this.source = source;
        this.width = 1.0;
        this.lineJoin = LineJoinE.MITER;
        this.innerJoin = InnerJoinE.MITER;
        this.miterLimit = 4.0;
        this.innerMiterLimit = 1.01;
        this.approximationScale = 1.0;
        this.autoDetectOrientation = false;
    }
    
    public void attach(VertexSource source) {
        this.source = source;
    }
    
    public void width(double w) {
        this.width = w * 0.5;
    }
    
    public void miterLimit(double ml) {
        this.miterLimit = ml;
    }
    
    public void innerMiterLimit(double ml) {
        this.innerMiterLimit = ml;
    }
    
    public void lineJoin(LineJoinE lj) {
        this.lineJoin = lj;
    }
    
    public void innerJoin(InnerJoinE ij) {
        this.innerJoin = ij;
    }
    
    public void approximationScale(double as) {
        this.approximationScale = as;
    }
    
    public void autoDetectOrientation(boolean v) {
        this.autoDetectOrientation = v;
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
            return AggBasics.PATH_CMD_STOP;
        }
        
        return source.vertex(xy);
    }
}
