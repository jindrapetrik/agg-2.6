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
// 2D Affine Transformation
//
//----------------------------------------------------------------------------

package agg;

/**
 * Simple 2D affine transformation matrix.
 * Simplified version of agg_trans_affine.h for basic transformations.
 */
public class Transform2D {
    
    // Transformation matrix:
    // | sx  shx  tx |
    // | shy sy   ty |
    // | 0   0    1  |
    
    public double sx;   // x scaling
    public double shy;  // y shearing
    public double shx;  // x shearing
    public double sy;   // y scaling
    public double tx;   // x translation
    public double ty;   // y translation
    
    /**
     * Default constructor - creates identity transformation.
     */
    public Transform2D() {
        sx = 1.0;
        shy = 0.0;
        shx = 0.0;
        sy = 1.0;
        tx = 0.0;
        ty = 0.0;
    }
    
    /**
     * Constructor with matrix values.
     * 
     * @param sx x scale
     * @param shy y shear
     * @param shx x shear
     * @param sy y scale
     * @param tx x translation
     * @param ty y translation
     */
    public Transform2D(double sx, double shy, double shx, 
                      double sy, double tx, double ty) {
        this.sx = sx;
        this.shy = shy;
        this.shx = shx;
        this.sy = sy;
        this.tx = tx;
        this.ty = ty;
    }
    
    /**
     * Copy constructor.
     * 
     * @param other transformation to copy
     */
    public Transform2D(Transform2D other) {
        this.sx = other.sx;
        this.shy = other.shy;
        this.shx = other.shx;
        this.sy = other.sy;
        this.tx = other.tx;
        this.ty = other.ty;
    }
    
    /**
     * Reset to identity transformation.
     * 
     * @return this transform for chaining
     */
    public Transform2D reset() {
        sx = sy = 1.0;
        shy = shx = tx = ty = 0.0;
        return this;
    }
    
    /**
     * Set translation.
     * 
     * @param x x translation
     * @param y y translation
     * @return this transform for chaining
     */
    public Transform2D translate(double x, double y) {
        tx += x;
        ty += y;
        return this;
    }
    
    /**
     * Set rotation (in radians).
     * 
     * @param angle rotation angle in radians
     * @return this transform for chaining
     */
    public Transform2D rotate(double angle) {
        double ca = Math.cos(angle);
        double sa = Math.sin(angle);
        double t0 = sx * ca - shy * sa;
        double t1 = shx * ca - sy * sa;
        shy = sx * sa + shy * ca;
        sy = shx * sa + sy * ca;
        sx = t0;
        shx = t1;
        return this;
    }
    
    /**
     * Set scaling.
     * 
     * @param x x scale factor
     * @param y y scale factor
     * @return this transform for chaining
     */
    public Transform2D scale(double x, double y) {
        sx *= x;
        shx *= x;
        shy *= y;
        sy *= y;
        return this;
    }
    
    /**
     * Set uniform scaling.
     * 
     * @param s scale factor
     * @return this transform for chaining
     */
    public Transform2D scale(double s) {
        return scale(s, s);
    }
    
    /**
     * Transform a point.
     * 
     * @param pt point to transform (modified in place)
     * @return the transformed point
     */
    public Point2D transform(Point2D pt) {
        double x = pt.x;
        double y = pt.y;
        pt.x = x * sx + y * shx + tx;
        pt.y = x * shy + y * sy + ty;
        return pt;
    }
    
    /**
     * Transform coordinates.
     * 
     * @param xy array with [x, y] coordinates (modified in place)
     */
    public void transform(double[] xy) {
        double x = xy[0];
        double y = xy[1];
        xy[0] = x * sx + y * shx + tx;
        xy[1] = x * shy + y * sy + ty;
    }
    
    /**
     * Inverse transform a point.
     * 
     * @param pt point to inverse transform (modified in place)
     * @return the inverse transformed point
     */
    public Point2D inverseTransform(Point2D pt) {
        double d = determinantReciprocal();
        double x = pt.x - tx;
        double y = pt.y - ty;
        pt.x = (x * sy - y * shx) * d;
        pt.y = (y * sx - x * shy) * d;
        return pt;
    }
    
    /**
     * Calculate determinant of the transformation matrix.
     * 
     * @return determinant value
     */
    public double determinant() {
        return sx * sy - shy * shx;
    }
    
    /**
     * Calculate reciprocal of determinant.
     * 
     * @return 1/determinant
     */
    public double determinantReciprocal() {
        return 1.0 / (sx * sy - shy * shx);
    }
    
    /**
     * Multiply this transformation with another (this = this * m).
     * 
     * @param m transformation to multiply with
     * @return this transform for chaining
     */
    public Transform2D multiply(Transform2D m) {
        double t0 = sx * m.sx + shy * m.shx;
        double t1 = shx * m.sx + sy * m.shx;
        double t2 = tx * m.sx + ty * m.shx + m.tx;
        shy = sx * m.shy + shy * m.sy;
        sy = shx * m.shy + sy * m.sy;
        ty = tx * m.shy + ty * m.sy + m.ty;
        sx = t0;
        shx = t1;
        tx = t2;
        return this;
    }
    
    /**
     * Invert this transformation.
     * 
     * @return this transform for chaining
     */
    public Transform2D invert() {
        double d = determinantReciprocal();
        double t0 = sy * d;
        sy = sx * d;
        shy = -shy * d;
        shx = -shx * d;
        double t4 = -tx * t0 - ty * shx;
        ty = -tx * shy - ty * sy;
        sx = t0;
        tx = t4;
        return this;
    }
    
    /**
     * Check if transformation is identity.
     * 
     * @param epsilon tolerance for comparison
     * @return true if approximately identity
     */
    public boolean isIdentity(double epsilon) {
        return Math.abs(sx - 1.0) <= epsilon &&
               Math.abs(shy) <= epsilon &&
               Math.abs(shx) <= epsilon &&
               Math.abs(sy - 1.0) <= epsilon &&
               Math.abs(tx) <= epsilon &&
               Math.abs(ty) <= epsilon;
    }
    
    @Override
    public String toString() {
        return String.format("Transform2D[%.4f %.4f %.4f %.4f %.4f %.4f]",
            sx, shy, shx, sy, tx, ty);
    }
    
    /**
     * Create translation transformation.
     * 
     * @param x x translation
     * @param y y translation
     * @return new transformation
     */
    public static Transform2D translation(double x, double y) {
        Transform2D t = new Transform2D();
        t.tx = x;
        t.ty = y;
        return t;
    }
    
    /**
     * Create rotation transformation.
     * 
     * @param angle rotation angle in radians
     * @return new transformation
     */
    public static Transform2D rotation(double angle) {
        Transform2D t = new Transform2D();
        t.sx = Math.cos(angle);
        t.shx = -Math.sin(angle);
        t.shy = Math.sin(angle);
        t.sy = Math.cos(angle);
        return t;
    }
    
    /**
     * Create scaling transformation.
     * 
     * @param sx x scale
     * @param sy y scale
     * @return new transformation
     */
    public static Transform2D scaling(double sx, double sy) {
        Transform2D t = new Transform2D();
        t.sx = sx;
        t.sy = sy;
        return t;
    }
}
