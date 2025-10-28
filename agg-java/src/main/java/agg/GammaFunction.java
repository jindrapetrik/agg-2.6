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
 * Gamma correction function interface.
 */
public interface GammaFunction {
    /**
     * Apply gamma correction to a value.
     * 
     * @param x input value (typically 0.0 to 1.0)
     * @return corrected value
     */
    double apply(double x);
}
