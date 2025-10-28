package agg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Compound shape that reads Flash-style vector data from a file.
 * Supports path commands with fill and stroke styles.
 * Based on compound_shape from flash_rasterizer.cpp
 */
public class CompoundShape implements VertexSource {
    private PathStorage path;
    private Transform2D affine;
    private ConvCurve curve;
    private ConvTransform trans;
    private List<PathStyle> styles;
    private BufferedReader reader;
    
    public CompoundShape() {
        this.path = new PathStorage();
        this.affine = new Transform2D();
        this.curve = new ConvCurve(path);
        this.trans = new ConvTransform(curve, affine);
        this.styles = new ArrayList<>();
    }
    
    public boolean open(String filename) {
        try {
            this.reader = new BufferedReader(new FileReader(filename));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // Ignore
            }
            reader = null;
        }
    }
    
    public boolean readNext() {
        if (reader == null) {
            return false;
        }
        
        path.removeAll();
        styles.clear();
        
        try {
            String line;
            
            // Find the start marker
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("=")) {
                    break;
                }
            }
            
            if (line == null) {
                return false;
            }
            
            // Read path commands until end marker
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("!")) {
                    break;
                }
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] tokens = line.split("\\s+");
                
                if (tokens[0].equals("Path")) {
                    // Path leftFill rightFill line ax ay
                    PathStyle style = new PathStyle();
                    style.pathId = path.startNewPath();
                    style.leftFill = Integer.parseInt(tokens[1]);
                    style.rightFill = Integer.parseInt(tokens[2]);
                    style.line = Integer.parseInt(tokens[3]);
                    double ax = Double.parseDouble(tokens[4]);
                    double ay = Double.parseDouble(tokens[5]);
                    path.moveTo(ax, ay);
                    styles.add(style);
                } else if (tokens[0].equals("Curve")) {
                    // Curve cx cy ax ay
                    double cx = Double.parseDouble(tokens[1]);
                    double cy = Double.parseDouble(tokens[2]);
                    double ax = Double.parseDouble(tokens[3]);
                    double ay = Double.parseDouble(tokens[4]);
                    path.curve3(cx, cy, ax, ay);
                } else if (tokens[0].equals("Line")) {
                    // Line ax ay
                    double ax = Double.parseDouble(tokens[1]);
                    double ay = Double.parseDouble(tokens[2]);
                    path.lineTo(ax, ay);
                } else if (tokens[0].startsWith("<")) {
                    // End of path marker
                    continue;
                }
            }
            
            return !styles.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
    
    public int paths() {
        return styles.size();
    }
    
    public PathStyle style(int i) {
        return styles.get(i);
    }
    
    public int getPathId(int i) {
        return styles.get(i).pathId;
    }
    
    public void scale(double w, double h) {
        affine.reset();
        
        // Get bounding rectangle
        double[] bounds = new double[4];
        if (BoundingRect.boundingRect(path, this, 0, styles.size(), bounds)) {
            double x1 = bounds[0];
            double y1 = bounds[1];
            double x2 = bounds[2];
            double y2 = bounds[3];
            
            if (x1 < x2 && y1 < y2) {
                // Calculate scale to fit in viewport
                double scaleX = w / (x2 - x1);
                double scaleY = h / (y2 - y1);
                double scale = Math.min(scaleX, scaleY) * 0.8; // 80% to leave margin
                
                // Center in viewport
                double tx = (w - (x2 - x1) * scale) / 2.0 - x1 * scale;
                double ty = (h - (y2 - y1) * scale) / 2.0 - y1 * scale;
                
                affine.scale(scale);
                affine.translate(tx, ty);
            }
        }
        
        curve.approximationScale(affine.getScale());
    }
    
    public void approximationScale(double s) {
        curve.approximationScale(affine.getScale() * s);
    }
    
    public double getScale() {
        return affine.getScale();
    }
    
    @Override
    public void rewind(int pathId) {
        trans.rewind(pathId);
    }
    
    @Override
    public int vertex(double[] xy) {
        return trans.vertex(xy);
    }
}
