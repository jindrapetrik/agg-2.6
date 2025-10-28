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
// Gamma correction functions
//
//----------------------------------------------------------------------------

package agg;

/**
 * Collection of gamma correction function implementations.
 * Java translation of agg_gamma_functions.h
 */
public final class GammaFunctions {
    
    private GammaFunctions() {}
    
    /**
     * No gamma correction - identity function.
     */
    public static class GammaNone implements GammaFunction {
        @Override
        public double apply(double x) {
            return x;
        }
    }
    
    /**
     * Power-based gamma correction.
     */
    public static class GammaPower implements GammaFunction {
        private double gamma;
        
        public GammaPower() {
            this.gamma = 1.0;
        }
        
        public GammaPower(double gamma) {
            this.gamma = gamma;
        }
        
        public void setGamma(double gamma) {
            this.gamma = gamma;
        }
        
        public double getGamma() {
            return gamma;
        }
        
        @Override
        public double apply(double x) {
            return Math.pow(x, gamma);
        }
    }
    
    /**
     * Threshold-based gamma correction.
     * Values below threshold become 0, values above become 1.
     */
    public static class GammaThreshold implements GammaFunction {
        private double threshold;
        
        public GammaThreshold() {
            this.threshold = 0.5;
        }
        
        public GammaThreshold(double threshold) {
            this.threshold = threshold;
        }
        
        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
        
        public double getThreshold() {
            return threshold;
        }
        
        @Override
        public double apply(double x) {
            return (x < threshold) ? 0.0 : 1.0;
        }
    }
    
    /**
     * Linear gamma correction with start and end points.
     */
    public static class GammaLinear implements GammaFunction {
        private double start;
        private double end;
        
        public GammaLinear() {
            this.start = 0.0;
            this.end = 1.0;
        }
        
        public GammaLinear(double start, double end) {
            this.start = start;
            this.end = end;
        }
        
        public void set(double start, double end) {
            this.start = start;
            this.end = end;
        }
        
        public void setStart(double start) {
            this.start = start;
        }
        
        public void setEnd(double end) {
            this.end = end;
        }
        
        public double getStart() {
            return start;
        }
        
        public double getEnd() {
            return end;
        }
        
        @Override
        public double apply(double x) {
            if (x < start) return 0.0;
            if (x > end) return 1.0;
            return (x - start) / (end - start);
        }
    }
    
    /**
     * Multiply gamma correction with clamping.
     */
    public static class GammaMultiply implements GammaFunction {
        private double multiplier;
        
        public GammaMultiply() {
            this.multiplier = 1.0;
        }
        
        public GammaMultiply(double multiplier) {
            this.multiplier = multiplier;
        }
        
        public void setValue(double multiplier) {
            this.multiplier = multiplier;
        }
        
        public double getValue() {
            return multiplier;
        }
        
        @Override
        public double apply(double x) {
            double y = x * multiplier;
            if (y > 1.0) y = 1.0;
            return y;
        }
    }
    
    /**
     * Convert sRGB color space to linear color space.
     * 
     * @param x sRGB value (0.0 to 1.0)
     * @return linear value
     */
    public static double sRGBToLinear(double x) {
        return (x <= 0.04045) ? (x / 12.92) : Math.pow((x + 0.055) / 1.055, 2.4);
    }
    
    /**
     * Convert linear color space to sRGB color space.
     * 
     * @param x linear value (0.0 to 1.0)
     * @return sRGB value
     */
    public static double linearToSRGB(double x) {
        return (x <= 0.0031308) ? (x * 12.92) : (1.055 * Math.pow(x, 1.0 / 2.4) - 0.055);
    }
}
