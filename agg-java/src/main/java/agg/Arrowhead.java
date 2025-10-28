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
// Simple arrowhead/arrowtail generator 
//
//----------------------------------------------------------------------------

package agg;

import static agg.AggBasics.*;

/**
 * Simple arrowhead/arrowtail generator.
 * Java translation of agg_arrowhead.h and agg_arrowhead.cpp
 */
public class Arrowhead {
    
    private double headD1;
    private double headD2;
    private double headD3;
    private double headD4;
    private double tailD1;
    private double tailD2;
    private double tailD3;
    private double tailD4;
    private boolean headFlag;
    private boolean tailFlag;
    private double[] coord;
    private int[] cmd;
    private int currId;
    private int currCoord;
    
    /**
     * Default constructor.
     */
    public Arrowhead() {
        this.headD1 = 1.0;
        this.headD2 = 1.0;
        this.headD3 = 1.0;
        this.headD4 = 0.0;
        this.tailD1 = 1.0;
        this.tailD2 = 1.0;
        this.tailD3 = 1.0;
        this.tailD4 = 0.0;
        this.headFlag = false;
        this.tailFlag = false;
        this.coord = new double[16];
        this.cmd = new int[8];
        this.currId = 0;
        this.currCoord = 0;
    }
    
    /**
     * Set head dimensions.
     * 
     * @param d1 head dimension 1
     * @param d2 head dimension 2
     * @param d3 head dimension 3
     * @param d4 head dimension 4
     */
    public void head(double d1, double d2, double d3, double d4) {
        this.headD1 = d1;
        this.headD2 = d2;
        this.headD3 = d3;
        this.headD4 = d4;
        this.headFlag = true;
    }
    
    /**
     * Enable head.
     */
    public void head() {
        this.headFlag = true;
    }
    
    /**
     * Disable head.
     */
    public void noHead() {
        this.headFlag = false;
    }
    
    /**
     * Set tail dimensions.
     * 
     * @param d1 tail dimension 1
     * @param d2 tail dimension 2
     * @param d3 tail dimension 3
     * @param d4 tail dimension 4
     */
    public void tail(double d1, double d2, double d3, double d4) {
        this.tailD1 = d1;
        this.tailD2 = d2;
        this.tailD3 = d3;
        this.tailD4 = d4;
        this.tailFlag = true;
    }
    
    /**
     * Enable tail.
     */
    public void tail() {
        this.tailFlag = true;
    }
    
    /**
     * Disable tail.
     */
    public void noTail() {
        this.tailFlag = false;
    }
    
    /**
     * Rewind the path to the beginning.
     * 
     * @param pathId path identifier (0 = tail, 1 = head)
     */
    public void rewind(int pathId) {
        this.currId = pathId;
        this.currCoord = 0;
        
        if (pathId == 0) {
            if (!tailFlag) {
                cmd[0] = PATH_CMD_STOP;
                return;
            }
            coord[0] = tailD1;
            coord[1] = 0.0;
            coord[2] = tailD1 - tailD4;
            coord[3] = tailD3;
            coord[4] = -tailD2 - tailD4;
            coord[5] = tailD3;
            coord[6] = -tailD2;
            coord[7] = 0.0;
            coord[8] = -tailD2 - tailD4;
            coord[9] = -tailD3;
            coord[10] = tailD1 - tailD4;
            coord[11] = -tailD3;
            
            cmd[0] = PATH_CMD_MOVE_TO;
            cmd[1] = PATH_CMD_LINE_TO;
            cmd[2] = PATH_CMD_LINE_TO;
            cmd[3] = PATH_CMD_LINE_TO;
            cmd[4] = PATH_CMD_LINE_TO;
            cmd[5] = PATH_CMD_LINE_TO;
            cmd[6] = PATH_CMD_STOP;
            cmd[7] = PATH_CMD_END_POLY | PATH_FLAGS_CLOSE | PATH_FLAGS_CCW;
            return;
        }
        
        if (pathId == 1) {
            if (!headFlag) {
                cmd[0] = PATH_CMD_STOP;
                return;
            }
            coord[0] = -headD1;
            coord[1] = 0.0;
            coord[2] = headD2 + headD4;
            coord[3] = -headD3;
            coord[4] = headD2;
            coord[5] = 0.0;
            coord[6] = headD2 + headD4;
            coord[7] = headD3;
            
            cmd[0] = PATH_CMD_MOVE_TO;
            cmd[1] = PATH_CMD_LINE_TO;
            cmd[2] = PATH_CMD_LINE_TO;
            cmd[3] = PATH_CMD_LINE_TO;
            cmd[4] = PATH_CMD_END_POLY | PATH_FLAGS_CLOSE | PATH_FLAGS_CCW;
            cmd[5] = PATH_CMD_STOP;
        }
    }
    
    /**
     * Get the next vertex in the arrow path.
     * 
     * @param xy array to receive x and y coordinates (must have length >= 2)
     * @return path command
     */
    public int vertex(double[] xy) {
        if (currId < 2) {
            int currIdx = currCoord * 2;
            xy[0] = coord[currIdx];
            xy[1] = coord[currIdx + 1];
            return cmd[currCoord++];
        }
        return PATH_CMD_STOP;
    }
}
