package agg;

/**
 * Vertex generator for stroke (outline) generation.
 * Generates outline vertices for paths with configurable line width and join/cap styles.
 * Based on agg_vcgen_stroke.h from the C++ AGG library.
 */
public class VcgenStroke {
    private double width = 0.5;
    private LineCapE lineCap = LineCapE.BUTT;
    private LineJoinE lineJoin = LineJoinE.MITER;
    private InnerJoinE innerJoin = InnerJoinE.MITER;
    private double miterLimit = 4.0;
    private double approximationScale = 1.0;
    
    public VcgenStroke() {
    }
    
    public void width(double w) {
        this.width = w;
    }
    
    public double width() {
        return width;
    }
    
    public void lineCap(LineCapE lc) {
        this.lineCap = lc;
    }
    
    public LineCapE lineCap() {
        return lineCap;
    }
    
    public void lineJoin(LineJoinE lj) {
        this.lineJoin = lj;
    }
    
    public LineJoinE lineJoin() {
        return lineJoin;
    }
    
    public void innerJoin(InnerJoinE ij) {
        this.innerJoin = ij;
    }
    
    public InnerJoinE innerJoin() {
        return innerJoin;
    }
    
    public void miterLimit(double ml) {
        this.miterLimit = ml;
    }
    
    public double miterLimit() {
        return miterLimit;
    }
    
    public void approximationScale(double as) {
        this.approximationScale = as;
    }
    
    public double approximationScale() {
        return approximationScale;
    }
}
