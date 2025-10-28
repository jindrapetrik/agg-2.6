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
// Liang-Barsky line clipping 
//
//----------------------------------------------------------------------------

package agg;

/**
 * Liang-Barsky line clipping algorithm implementation.
 * Java translation of agg_clip_liang_barsky.h
 */
public final class ClipLiangBarsky {
    
    // Private constructor to prevent instantiation
    private ClipLiangBarsky() {}
    
    // Clipping flags constants
    public static final int CLIPPING_FLAGS_X1_CLIPPED = 4;
    public static final int CLIPPING_FLAGS_X2_CLIPPED = 1;
    public static final int CLIPPING_FLAGS_Y1_CLIPPED = 8;
    public static final int CLIPPING_FLAGS_Y2_CLIPPED = 2;
    public static final int CLIPPING_FLAGS_X_CLIPPED = CLIPPING_FLAGS_X1_CLIPPED | CLIPPING_FLAGS_X2_CLIPPED;
    public static final int CLIPPING_FLAGS_Y_CLIPPED = CLIPPING_FLAGS_Y1_CLIPPED | CLIPPING_FLAGS_Y2_CLIPPED;
    
    /**
     * Determine the clipping code of the vertex according to the 
     * Cyrus-Beck line clipping algorithm.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param clipBox clipping rectangle
     * @return clipping flags
     */
    public static int clippingFlags(double x, double y, RectD clipBox) {
        return (x > clipBox.x2 ? 1 : 0) |
               ((y > clipBox.y2 ? 1 : 0) << 1) |
               ((x < clipBox.x1 ? 1 : 0) << 2) |
               ((y < clipBox.y1 ? 1 : 0) << 3);
    }
    
    /**
     * Get clipping flags for x coordinate only.
     * 
     * @param x x coordinate
     * @param clipBox clipping rectangle
     * @return clipping flags for x
     */
    public static int clippingFlagsX(double x, RectD clipBox) {
        return (x > clipBox.x2 ? 1 : 0) | ((x < clipBox.x1 ? 1 : 0) << 2);
    }
    
    /**
     * Get clipping flags for y coordinate only.
     * 
     * @param y y coordinate
     * @param clipBox clipping rectangle
     * @return clipping flags for y
     */
    public static int clippingFlagsY(double y, RectD clipBox) {
        return ((y > clipBox.y2 ? 1 : 0) << 1) | ((y < clipBox.y1 ? 1 : 0) << 3);
    }
    
    /**
     * Clip a line segment using Liang-Barsky algorithm.
     * 
     * @param x1 start x coordinate
     * @param y1 start y coordinate
     * @param x2 end x coordinate
     * @param y2 end y coordinate
     * @param clipBox clipping rectangle
     * @param outPoints output array for clipped points (must have length >= 4)
     * @return number of output points (0, 2, or 4)
     */
    public static int clipLiangBarsky(double x1, double y1, double x2, double y2,
                                      RectD clipBox, double[] outPoints) {
        final double NEARZERO = 1e-30;
        
        double deltax = x2 - x1;
        double deltay = y2 - y1;
        double xin;
        double xout;
        double yin;
        double yout;
        double tinx;
        double tiny;
        double toutx;
        double touty;
        double tin1;
        double tin2;
        double tout1;
        int np = 0;
        
        if (deltax == 0.0) {
            // bump off of the vertical
            deltax = (x1 > clipBox.x1) ? -NEARZERO : NEARZERO;
        }
        
        if (deltay == 0.0) {
            // bump off of the horizontal
            deltay = (y1 > clipBox.y1) ? -NEARZERO : NEARZERO;
        }
        
        if (deltax > 0.0) {
            // points to right
            xin = clipBox.x1;
            xout = clipBox.x2;
        } else {
            xin = clipBox.x2;
            xout = clipBox.x1;
        }
        
        if (deltay > 0.0) {
            // points up
            yin = clipBox.y1;
            yout = clipBox.y2;
        } else {
            yin = clipBox.y2;
            yout = clipBox.y1;
        }
        
        tinx = (xin - x1) / deltax;
        tiny = (yin - y1) / deltay;
        
        if (tinx < tiny) {
            // hits x first
            tin1 = tinx;
            tin2 = tiny;
        } else {
            // hits y first
            tin1 = tiny;
            tin2 = tinx;
        }
        
        if (tin1 <= 1.0) {
            if (0.0 < tin1) {
                outPoints[np * 2] = xin;
                outPoints[np * 2 + 1] = yin;
                np++;
            }
            
            if (tin2 <= 1.0) {
                toutx = (xout - x1) / deltax;
                touty = (yout - y1) / deltay;
                
                tout1 = (toutx < touty) ? toutx : touty;
                
                if (tin2 > 0.0 || tout1 > 0.0) {
                    if (tin2 <= tout1) {
                        if (tin2 > 0.0) {
                            if (tinx > tiny) {
                                outPoints[np * 2] = xin;
                                outPoints[np * 2 + 1] = y1 + tinx * deltay;
                            } else {
                                outPoints[np * 2] = x1 + tiny * deltax;
                                outPoints[np * 2 + 1] = yin;
                            }
                            np++;
                        }
                        
                        if (tout1 < 1.0) {
                            if (toutx < touty) {
                                outPoints[np * 2] = xout;
                                outPoints[np * 2 + 1] = y1 + toutx * deltay;
                            } else {
                                outPoints[np * 2] = x1 + touty * deltax;
                                outPoints[np * 2 + 1] = yout;
                            }
                        } else {
                            outPoints[np * 2] = x2;
                            outPoints[np * 2 + 1] = y2;
                        }
                        np++;
                    } else {
                        if (tinx > tiny) {
                            outPoints[np * 2] = xin;
                            outPoints[np * 2 + 1] = yout;
                        } else {
                            outPoints[np * 2] = xout;
                            outPoints[np * 2 + 1] = yin;
                        }
                        np++;
                    }
                }
            }
        }
        return np;
    }
    
    /**
     * Helper class to hold line segment coordinates for clipping.
     */
    public static class LineSegment {
        public double x1, y1, x2, y2;
        
        public LineSegment(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    /**
     * Clip a line segment to a rectangle.
     * Returns: ret >= 4 - Fully clipped
     *          (ret & 1) != 0 - First point has been moved
     *          (ret & 2) != 0 - Second point has been moved
     * 
     * @param line line segment to clip (modified in place)
     * @param clipBox clipping rectangle
     * @return clipping result flags
     */
    public static int clipLineSegment(LineSegment line, RectD clipBox) {
        int f1 = clippingFlags(line.x1, line.y1, clipBox);
        int f2 = clippingFlags(line.x2, line.y2, clipBox);
        int ret = 0;
        
        if ((f2 | f1) == 0) {
            // Fully visible
            return 0;
        }
        
        if ((f1 & CLIPPING_FLAGS_X_CLIPPED) != 0 &&
            (f1 & CLIPPING_FLAGS_X_CLIPPED) == (f2 & CLIPPING_FLAGS_X_CLIPPED)) {
            // Fully clipped
            return 4;
        }
        
        if ((f1 & CLIPPING_FLAGS_Y_CLIPPED) != 0 &&
            (f1 & CLIPPING_FLAGS_Y_CLIPPED) == (f2 & CLIPPING_FLAGS_Y_CLIPPED)) {
            // Fully clipped
            return 4;
        }
        
        double tx1 = line.x1;
        double ty1 = line.y1;
        double tx2 = line.x2;
        double ty2 = line.y2;
        
        if (f1 != 0) {
            double[] pt = new double[2];
            if (!clipMovePoint(tx1, ty1, tx2, ty2, clipBox, pt, f1)) {
                return 4;
            }
            line.x1 = pt[0];
            line.y1 = pt[1];
            if (line.x1 == line.x2 && line.y1 == line.y2) {
                return 4;
            }
            ret |= 1;
        }
        
        if (f2 != 0) {
            double[] pt = new double[2];
            if (!clipMovePoint(tx1, ty1, tx2, ty2, clipBox, pt, f2)) {
                return 4;
            }
            line.x2 = pt[0];
            line.y2 = pt[1];
            if (line.x1 == line.x2 && line.y1 == line.y2) {
                return 4;
            }
            ret |= 2;
        }
        
        return ret;
    }
    
    /**
     * Move a point to the clipping boundary.
     * 
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     * @param clipBox clipping rectangle
     * @param outPoint output point (array of length 2: x, y)
     * @param flags clipping flags
     * @return true if successful, false if line is degenerate
     */
    private static boolean clipMovePoint(double x1, double y1, double x2, double y2,
                                         RectD clipBox, double[] outPoint, int flags) {
        double bound;
        
        if ((flags & CLIPPING_FLAGS_X_CLIPPED) != 0) {
            if (x1 == x2) {
                return false;
            }
            bound = ((flags & CLIPPING_FLAGS_X1_CLIPPED) != 0) ? clipBox.x1 : clipBox.x2;
            outPoint[1] = (bound - x1) * (y2 - y1) / (x2 - x1) + y1;
            outPoint[0] = bound;
        } else {
            outPoint[0] = x1;
            outPoint[1] = y1;
        }
        
        flags = clippingFlagsY(outPoint[1], clipBox);
        if ((flags & CLIPPING_FLAGS_Y_CLIPPED) != 0) {
            if (y1 == y2) {
                return false;
            }
            bound = ((flags & CLIPPING_FLAGS_Y1_CLIPPED) != 0) ? clipBox.y1 : clipBox.y2;
            outPoint[0] = (bound - y1) * (x2 - x1) / (y2 - y1) + x1;
            outPoint[1] = bound;
        }
        
        return true;
    }
}
