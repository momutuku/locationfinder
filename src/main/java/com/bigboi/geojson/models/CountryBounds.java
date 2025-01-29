package com.bigboi.geojson.models;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public class CountryBounds {
    private double minLat = Double.MAX_VALUE;
    private double maxLat = Double.MIN_VALUE;
    private double minLon = Double.MAX_VALUE;
    private double maxLon = Double.MIN_VALUE;

    public void updateBounds(Geometry geometry) {
        Envelope envelope = geometry.getEnvelopeInternal();
        minLat = Math.min(minLat, envelope.getMinY());
        maxLat = Math.max(maxLat, envelope.getMaxY());
        minLon = Math.min(minLon, envelope.getMinX());
        maxLon = Math.max(maxLon, envelope.getMaxX());
    }

    public boolean containsPoint(double lat, double lon) {
        return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon;
    }
}
