package de.bluecolored.bluemap.api.markers;

import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;

public interface ShapeMarker extends Marker {
    static Builder builder() { return new Builder(); }

    class Builder {
        public Builder label(String l) { return this; }
        public Builder shape(Shape s, float y) { return this; }
        public Builder depthTestEnabled(boolean e) { return this; }
        public Builder fillColor(Color c) { return this; }
        public Builder lineColor(Color c) { return this; }
        public Builder lineWidth(int w) { return this; }
        public Builder detail(String d) { return this; }
        public ShapeMarker build() { return null; }
    }
}
