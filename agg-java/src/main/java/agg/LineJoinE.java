package agg;

/**
 * Line join styles for stroke rendering
 */
public enum LineJoinE {
    MITER,        // Miter join - extends lines until they meet
    MITER_REVERT, // Miter join with revert
    ROUND,        // Round join - circular arc
    BEVEL,        // Bevel join - straight line connecting ends
    MITER_ROUND;  // Miter with round fallback
}
