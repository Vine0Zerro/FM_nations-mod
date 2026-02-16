package de.bluecolored.bluemap.api.markers;

import java.util.Map;

public interface MarkerSet {
    Map<String, Marker> getMarkers();
    void put(String id, Marker marker);
    
    static Builder builder() { return new Builder(); }

    class Builder {
        public Builder label(String l) { return this; }
        public Builder defaultHidden(boolean h) { return this; }
        public MarkerSet build() { return null; }
    }
}
