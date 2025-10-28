# AGG Java - Anti-Grain Geometry for Java 8

This is a Java 8 port of the Anti-Grain Geometry (AGG) 2D graphics library, originally written in C++ by Maxim Shemanarev.

## About

Anti-Grain Geometry (AGG) is a high-quality 2D rendering library that produces pixel images in memory from vectorial data. This Java port maintains the core concepts and algorithms from the original C++ implementation while adapting them to Java's object-oriented paradigm and memory management.

## Project Structure

```
agg-java/
├── pom.xml                           # Maven build configuration
├── src/
│   └── main/
│       └── java/
│           └── agg/                  # Java package for AGG classes
│               ├── AggBasics.java    # Basic constants and path commands
│               ├── Arc.java          # Arc vertex generator
│               └── BSpline.java      # Bi-cubic spline interpolation
└── README.md                         # This file
```

## Key Features (Original AGG)

- Anti-Aliasing and Subpixel Accuracy
- High-quality rendering of arbitrary polygons
- Gradients and Gouraud Shading
- Image transformations with various interpolation filters
- Strokes with different line joins and caps
- Dashed line generation
- Alpha-Masking
- Boolean polygon operations

## Translation Status

This is an ongoing translation from C++ to Java 8. The following classes have been translated:

- [x] `AggBasics` - Basic types, constants, and path command utilities
- [x] `Arc` - Arc vertex generator
- [x] `BSpline` - Bi-cubic spline interpolation
- [x] `Arrowhead` - Arrowhead/arrowtail generator
- [x] `Rgba` - RGBA color representation
- [x] `Ellipse` - Ellipse vertex generator
- [x] `RoundedRect` - Rounded rectangle generator
- [x] `BezierArc` - Bezier arc approximation
- [x] `RectD` - Rectangle with double precision
- [x] `ClipLiangBarsky` - Line clipping utilities
- [x] `AggMath` - Math utility functions
- [x] `VertexDist` - Vertex with distance calculation
- [x] `GammaFunction` - Gamma correction interface
- [x] `GammaFunctions` - Gamma correction implementations
- [x] `VertexSource` - Interface for vertex sources
- [x] `BoundingRect` - Bounding rectangle utilities
- [x] `Point2D` - 2D point utility class
- [ ] Additional classes in progress...

**Progress: ~7.5% complete (18 classes from ~286 C++ files)**

### Original C++ Source

The original C++ source contains:
- 129 .cpp implementation files
- 157 .h header files

### Translation Approach

1. **Package Structure**: All classes are in the `agg` package
2. **Naming Conventions**: 
   - C++ `snake_case` functions → Java `camelCase` methods
   - C++ classes maintain similar names with Java naming conventions
3. **Memory Management**: C++ pointers and manual memory management → Java object references and automatic garbage collection
4. **Templates**: C++ templates → Java generics where applicable
5. **Namespaces**: C++ `namespace agg` → Java `package agg`

## Building

### Prerequisites

- Java 8 or higher
- Maven 3.x

### Build Commands

```bash
# Compile the project
mvn compile

# Run tests (when available)
mvn test

# Package as JAR
mvn package
```

## Usage Example

```java
import agg.Arc;
import static agg.AggBasics.*;

public class Example {
    public static void main(String[] args) {
        // Create an arc from (100, 100) with radius 50
        Arc arc = new Arc(100.0, 100.0, 50.0, 50.0, 
                         0.0, Math.PI / 2.0, true);
        
        // Generate vertices
        arc.rewind(0);
        double[] xy = new double[2];
        int cmd;
        
        while (!isStop(cmd = arc.vertex(xy))) {
            System.out.printf("x=%.2f, y=%.2f, cmd=%d%n", 
                            xy[0], xy[1], cmd);
        }
    }
}
```

## License

AGG 2.6 is dual licensed:
- Modified BSD License
- Anti-Grain Geometry Public License

Both licenses allow for free use in commercial software.

See the [License HTML](https://github.com/ghaerr/agg-2.6/blob/master/agg-web/license/index.html) for full license texts.

## Original Authors

- Maxim Shemanarev - Original C++ implementation
- Contact: mcseem@antigrain.com

## Java Translation

This Java port aims to provide the same high-quality rendering capabilities while leveraging Java's cross-platform capabilities and modern language features.

## Contributing

This is an ongoing translation project. Contributions are welcome for:
- Translating additional C++ classes
- Adding unit tests
- Performance optimizations
- Documentation improvements

## Resources

- [Original AGG Website](http://www.antigrain.com) (historical)
- [AGG on SourceForge](https://sourceforge.net/projects/agg/)
- [GitHub Mirror](https://github.com/ghaerr/agg-2.6)
