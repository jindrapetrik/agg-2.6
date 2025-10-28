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
// class BSpline - Bi-cubic Spline interpolation
//
//----------------------------------------------------------------------------

package agg;

/**
 * A very simple class of Bi-cubic Spline interpolation.
 * First call init(num, x[], y[]) where num - number of source points, 
 * x, y - arrays of X and Y values respectively. Here Y must be a function 
 * of X. It means that all the X-coordinates must be arranged in the ascending
 * order. 
 * Then call get(x) that calculates a value Y for the respective X. 
 * The class supports extrapolation, i.e. you can call get(x) where x is
 * outside the given with init() X-range. Extrapolation is a simple linear 
 * function.
 * 
 * Java translation of agg_bspline.h and agg_bspline.cpp
 */
public class BSpline {
    
    private int max;
    private int num;
    private double[] x;
    private double[] y;
    private double[] am;
    private int lastIdx;
    
    /**
     * Default constructor.
     */
    public BSpline() {
        this.max = 0;
        this.num = 0;
        this.x = null;
        this.y = null;
        this.lastIdx = -1;
    }
    
    /**
     * Constructor with maximum number of points.
     * 
     * @param num maximum number of points
     */
    public BSpline(int num) {
        this();
        init(num);
    }
    
    /**
     * Constructor with data points.
     * 
     * @param num number of points
     * @param x array of x coordinates
     * @param y array of y coordinates
     */
    public BSpline(int num, double[] x, double[] y) {
        this();
        init(num, x, y);
    }
    
    /**
     * Initialize with maximum number of points.
     * 
     * @param max maximum number of points
     */
    public void init(int max) {
        if (max > 2 && max > this.max) {
            this.am = new double[max * 3];
            this.max = max;
            this.x = new double[max];
            this.y = new double[max];
            
            // Point x and y to appropriate positions in am array
            System.arraycopy(am, max, x, 0, 0);
            System.arraycopy(am, max * 2, y, 0, 0);
        }
        this.num = 0;
        this.lastIdx = -1;
    }
    
    /**
     * Add a point to the spline.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(double x, double y) {
        if (num < max) {
            this.x[num] = x;
            this.y[num] = y;
            num++;
        }
    }
    
    /**
     * Prepare the spline for interpolation.
     * Must be called after all points are added.
     */
    public void prepare() {
        if (num > 2) {
            int k, n1;
            double h, p, d, f, e;
            
            for (k = 0; k < num; k++) {
                am[k] = 0.0;
            }
            
            n1 = 3 * num;
            double[] temp = new double[n1];
            
            for (k = 0; k < n1; k++) {
                temp[k] = 0.0;
            }
            
            double[] r = new double[num];
            double[] s = new double[num];
            double[] al = new double[num];
            
            System.arraycopy(temp, num, r, 0, num);
            System.arraycopy(temp, num * 2, s, 0, num);
            
            n1 = num - 1;
            d = x[1] - x[0];
            e = (y[1] - y[0]) / d;
            
            for (k = 1; k < n1; k++) {
                h = d;
                d = x[k + 1] - x[k];
                f = e;
                e = (y[k + 1] - y[k]) / d;
                al[k] = d / (d + h);
                r[k] = 1.0 - al[k];
                s[k] = 6.0 * (e - f) / (h + d);
            }
            
            for (k = 1; k < n1; k++) {
                p = 1.0 / (r[k] * al[k - 1] + 2.0);
                al[k] *= -p;
                s[k] = (s[k] - r[k] * s[k - 1]) * p;
            }
            
            am[n1] = 0.0;
            al[n1 - 1] = s[n1 - 1];
            am[n1 - 1] = al[n1 - 1];
            
            for (k = n1 - 2; k >= 0; k--) {
                al[k] = al[k] * al[k + 1] + s[k];
                am[k] = al[k];
            }
        }
        lastIdx = -1;
    }
    
    /**
     * Initialize with data points.
     * 
     * @param num number of points
     * @param x array of x coordinates
     * @param y array of y coordinates
     */
    public void init(int num, double[] x, double[] y) {
        if (num > 2) {
            init(num);
            for (int i = 0; i < num; i++) {
                addPoint(x[i], y[i]);
            }
            prepare();
        }
        lastIdx = -1;
    }
    
    /**
     * Binary search for the interval containing x.
     * 
     * @param n number of points
     * @param x array of x coordinates
     * @param x0 x value to search for
     * @return index of the interval
     */
    private int bsearch(int n, double[] x, double x0) {
        int i = 0;
        int j = n - 1;
        int k;
        
        while ((j - i) > 1) {
            k = (i + j) >> 1;
            if (x0 < x[k]) {
                j = k;
            } else {
                i = k;
            }
        }
        
        return i;
    }
    
    /**
     * Interpolate at x.
     * 
     * @param x x coordinate
     * @param i interval index
     * @return interpolated y value
     */
    private double interpolation(double x, int i) {
        int j = i + 1;
        double d = this.x[i] - this.x[j];
        double h = x - this.x[j];
        double r = this.x[i] - x;
        double p = d * d / 6.0;
        
        return (am[j] * r * r * r + am[i] * h * h * h) / 6.0 / d +
               ((y[j] - am[j] * p) * r + (y[i] - am[i] * p) * h) / d;
    }
    
    /**
     * Extrapolate to the left of the first point.
     * 
     * @param x x coordinate
     * @return extrapolated y value
     */
    private double extrapolationLeft(double x) {
        double d = this.x[1] - this.x[0];
        return (-d * am[1] / 6.0 + (y[1] - y[0]) / d) * (x - this.x[0]) + y[0];
    }
    
    /**
     * Extrapolate to the right of the last point.
     * 
     * @param x x coordinate
     * @return extrapolated y value
     */
    private double extrapolationRight(double x) {
        double d = this.x[num - 1] - this.x[num - 2];
        return (d * am[num - 2] / 6.0 + (y[num - 1] - y[num - 2]) / d) *
               (x - this.x[num - 1]) + y[num - 1];
    }
    
    /**
     * Get the interpolated y value at x.
     * 
     * @param x x coordinate
     * @return interpolated or extrapolated y value
     */
    public double get(double x) {
        if (num > 2) {
            // Extrapolation on the left
            if (x < this.x[0]) {
                return extrapolationLeft(x);
            }
            
            // Extrapolation on the right
            if (x >= this.x[num - 1]) {
                return extrapolationRight(x);
            }
            
            // Interpolation
            int i = bsearch(num, this.x, x);
            return interpolation(x, i);
        }
        return 0.0;
    }
    
    /**
     * Get the interpolated y value at x with state optimization.
     * This version remembers the last interval and tries to reuse it.
     * 
     * @param x x coordinate
     * @return interpolated or extrapolated y value
     */
    public double getStateful(double x) {
        if (num > 2) {
            // Extrapolation on the left
            if (x < this.x[0]) {
                return extrapolationLeft(x);
            }
            
            // Extrapolation on the right
            if (x >= this.x[num - 1]) {
                return extrapolationRight(x);
            }
            
            if (lastIdx >= 0) {
                // Check if x is not in current range
                if (x < this.x[lastIdx] || x > this.x[lastIdx + 1]) {
                    // Check if x between next points (most probably)
                    if (lastIdx < num - 2 &&
                        x >= this.x[lastIdx + 1] &&
                        x <= this.x[lastIdx + 2]) {
                        lastIdx++;
                    } else if (lastIdx > 0 &&
                               x >= this.x[lastIdx - 1] &&
                               x <= this.x[lastIdx]) {
                        // x is between previous points
                        lastIdx--;
                    } else {
                        // Else perform full search
                        lastIdx = bsearch(num, this.x, x);
                    }
                }
                return interpolation(x, lastIdx);
            } else {
                // Interpolation
                lastIdx = bsearch(num, this.x, x);
                return interpolation(x, lastIdx);
            }
        }
        return 0.0;
    }
}
