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
// Gamma lookup table for fast gamma correction
//
//----------------------------------------------------------------------------

package agg;

/**
 * Gamma lookup table for fast gamma correction.
 * Simplified Java translation of agg_gamma_lut.h
 */
public class GammaLut {
    
    public static final int GAMMA_SHIFT = 8;
    public static final int GAMMA_SIZE = 1 << GAMMA_SHIFT;  // 256
    public static final int GAMMA_MASK = GAMMA_SIZE - 1;     // 255
    
    private double gamma;
    private int[] dirGamma;  // Direct gamma table
    private int[] invGamma;  // Inverse gamma table
    
    /**
     * Default constructor - creates identity gamma (1.0).
     */
    public GammaLut() {
        this(1.0);
    }
    
    /**
     * Constructor with gamma value.
     * 
     * @param gamma gamma correction value
     */
    public GammaLut(double gamma) {
        this.dirGamma = new int[GAMMA_SIZE];
        this.invGamma = new int[GAMMA_SIZE];
        setGamma(gamma);
    }
    
    /**
     * Set gamma correction value and rebuild lookup tables.
     * 
     * @param g gamma value
     */
    public void setGamma(double g) {
        this.gamma = g;
        
        // Build direct gamma table
        for (int i = 0; i < GAMMA_SIZE; i++) {
            dirGamma[i] = AggMath.uround(
                Math.pow(i / (double)GAMMA_MASK, gamma) * GAMMA_MASK
            );
        }
        
        // Build inverse gamma table
        double invG = 1.0 / g;
        for (int i = 0; i < GAMMA_SIZE; i++) {
            invGamma[i] = AggMath.uround(
                Math.pow(i / (double)GAMMA_MASK, invG) * GAMMA_MASK
            );
        }
    }
    
    /**
     * Get current gamma value.
     * 
     * @return gamma value
     */
    public double getGamma() {
        return gamma;
    }
    
    /**
     * Apply direct gamma correction to a value.
     * 
     * @param v input value (0-255)
     * @return gamma corrected value (0-255)
     */
    public int dir(int v) {
        return dirGamma[Math.max(0, Math.min(GAMMA_MASK, v))];
    }
    
    /**
     * Apply inverse gamma correction to a value.
     * 
     * @param v input value (0-255)
     * @return inverse gamma corrected value (0-255)
     */
    public int inv(int v) {
        return invGamma[Math.max(0, Math.min(GAMMA_MASK, v))];
    }
    
    /**
     * Get direct gamma lookup table.
     * 
     * @return direct gamma table array
     */
    public int[] getDirTable() {
        return dirGamma;
    }
    
    /**
     * Get inverse gamma lookup table.
     * 
     * @return inverse gamma table array
     */
    public int[] getInvTable() {
        return invGamma;
    }
}
