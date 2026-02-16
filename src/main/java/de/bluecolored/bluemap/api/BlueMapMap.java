package de.bluecolored.bluemap.api;

import de.bluecolored.bluemap.api.markers.MarkerSet;
import java.util.Map;

public interface BlueMapMap {
    String getId();
    Map<String, MarkerSet> getMarkerSets();
}
