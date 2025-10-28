package agg;

import java.util.ArrayList;
import java.util.List;
import static agg.AggBasics.*;

/**
 * Path reversal vertex source.
 * When iterating over a path, this class reverses the direction
 * which is needed for proper compound rasterization when fill is on the right side.
 * 
 * Similar to the approach used in AGG's compound rasterizer.
 */
public class PathReverse implements VertexSource {
    private VertexSource source;
    private int pathId;
    private List<Double> verticesX;
    private List<Double> verticesY;
    private List<Integer> commands;
    private int currentIndex;
    
    public PathReverse(VertexSource source) {
        this.source = source;
        verticesX = new ArrayList<>();
        verticesY = new ArrayList<>();
        commands = new ArrayList<>();
    }
    
    @Override
    public void rewind(int pathId) {
        this.pathId = pathId;
        currentIndex = 0;
        
        // Read all vertices from the source path
        verticesX.clear();
        verticesY.clear();
        commands.clear();
        
        source.rewind(pathId);
        double[] coords = new double[2];
        int cmd;
        
        while (!isStop(cmd = source.vertex(coords))) {
            verticesX.add(coords[0]);
            verticesY.add(coords[1]);
            commands.add(cmd);
        }
        
        // Reverse the vertices (except move_to which stays at start)
        if (commands.size() > 1) {
            // Keep the first move_to at the beginning
            int startIdx = 0;
            if (isMoveTo(commands.get(0))) {
                startIdx = 1;
            }
            
            // Reverse the rest
            int endIdx = commands.size() - 1;
            while (startIdx < endIdx) {
                // Swap vertices
                double tmpX = verticesX.get(startIdx);
                double tmpY = verticesY.get(startIdx);
                verticesX.set(startIdx, verticesX.get(endIdx));
                verticesY.set(startIdx, verticesY.get(endIdx));
                verticesX.set(endIdx, tmpX);
                verticesY.set(endIdx, tmpY);
                
                // Swap commands
                int tmpCmd = commands.get(startIdx);
                commands.set(startIdx, commands.get(endIdx));
                commands.set(endIdx, tmpCmd);
                
                startIdx++;
                endIdx--;
            }
            
            // Convert the first vertex after move_to back to line_to
            // (it was swapped from the end)
            if (commands.size() > 1 && isMoveTo(commands.get(0))) {
                int firstAfterMove = 1;
                if (firstAfterMove < commands.size()) {
                    commands.set(firstAfterMove, PATH_CMD_LINE_TO);
                }
            }
        }
    }
    
    @Override
    public int vertex(double[] coords) {
        if (currentIndex >= commands.size()) {
            return PATH_CMD_STOP;
        }
        
        coords[0] = verticesX.get(currentIndex);
        coords[1] = verticesY.get(currentIndex);
        int cmd = commands.get(currentIndex);
        currentIndex++;
        
        return cmd;
    }
}
