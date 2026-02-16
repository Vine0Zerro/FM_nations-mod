package de.bluecolored.bluemap.api.markers;

public interface POIMarker extends Marker {
    static Builder toBuilder() { return new Builder(); }

    class Builder {
        public Builder label(String l) { return this; }
        public Builder position(double x, double y, double z) { return this; }
        public Builder detail(String d) { return this; }
        public POIMarker build() { return null; }
    }
}
