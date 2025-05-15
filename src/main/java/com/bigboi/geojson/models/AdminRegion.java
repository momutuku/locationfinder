package com.bigboi.geojson.models;

import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.MultiPolygon;

public class AdminRegion {
    private final MultiPolygon geometry;
    private final Map<String, String> properties;
    private final Map<String, Object> originalProperties;

    public AdminRegion(MultiPolygon geometry, Map<String, String> properties, Map<String, Object> originalProperties) {
        this.geometry = geometry;
        this.properties = properties;
        this.originalProperties = originalProperties;
    }

    public MultiPolygon getGeometry() {
        return geometry;
    }

    public Map<String, String> getProperties() {
        return new TreeMap<>(properties);
    }

    public Map<String, Object> getOriginalProperties() {
        return originalProperties;
    }
}
