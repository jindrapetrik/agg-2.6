# Compound Rasterizer Implementation Plan

## Overview
Translating AGG's RasterizerCompoundAa (~665 lines C++) to Java for perfect polygon stitching in Flash compound shapes.

## Core Concept
Unlike simple rasterization which treats each path independently, compound rasterization:
1. Tracks LEFT and RIGHT fill styles for each cell
2. Groups cells by style during scanline sweep  
3. Blends overlapping styles per pixel
4. Achieves perfect stitching at polygon boundaries

## Implementation Phases

### Phase 1: Data Structures ✅ COMPLETE
- [x] CellStyleAa - Cell with left/right style indices
- [x] StyleInfo - Per-style cell tracking  
- [x] CellInfo - Simplified cell for rendering

### Phase 2: RasterizerCompoundAa Core (IN PROGRESS)
- [ ] Basic structure and fields
- [ ] styles() method - Set left/right fills for edges
- [ ] add_path() / add_vertex() - Path addition
- [ ] sort() / rewind_scanlines() - Preparation

### Phase 3: Scanline Sweeping  
- [ ] sweep_styles() - Group cells by style for current scanline
- [ ] sweep_scanline() - Extract cells for specific style
- [ ] style() - Get style ID from index

### Phase 4: Supporting Classes
- [ ] ScanlineBin - Binary scanline (no AA data, just spans)
- [ ] SpanAllocator - Memory allocation for color spans
- [ ] StyleHandler - Interface for color lookup by style

### Phase 5: Rendering
- [ ] render_scanlines_compound() - Main rendering method
- [ ] Color blending logic for overlapping styles
- [ ] Integration with existing renderer

### Phase 6: Integration
- [ ] Update FlashRasterizerExample to use compound rasterizer
- [ ] Remove flash_rasterizer2 approach  
- [ ] Test with Flash shapes

## Key Challenges

### 1. Cell Management
C++ uses `cell_style_aa` cells stored in `rasterizer_cells_aa<cell_style_aa>`.
Java solution: Extend RasterizerCellsAa to work with CellStyleAa.

### 2. Style Tracking
Compound rasterizer maintains:
- m_styles: Array of StyleInfo (one per fill index)
- m_ast: Active Style Table (unique styles in scanline)
- m_asm: Active Style Mask (bitmap of active styles)
- m_cells: Array of CellInfo (cells grouped by style)

### 3. Two-Pass Rendering
For multiple styles on same scanline:
1. Sweep binary scanline to get span coverage
2. Clear mix_buffer for those spans
3. For each style: sweep AA scanline, blend into mix_buffer  
4. Render final mix_buffer to output

## Estimated Scope
- RasterizerCompoundAa: ~400 lines
- ScanlineBin: ~100 lines
- SpanAllocator: ~50 lines
- StyleHandler: ~30 lines  
- render_scanlines_compound: ~150 lines
- Updates/Integration: ~100 lines
**Total: ~830 lines new Java code**

## Current Status
✅ Phase 1 complete (data structures)
🔄 Phase 2 in progress (rasterizer core)

## Next Steps
1. Create RasterizerCompoundAa skeleton
2. Implement styles() and path addition
3. Implement sweep_styles() - most complex part
4. Create supporting classes
5. Implement render_scanlines_compound
6. Test and refine
