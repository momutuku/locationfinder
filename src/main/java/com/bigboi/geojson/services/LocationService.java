package com.bigboi.geojson.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.bigboi.geojson.models.AdminRegion;
import com.bigboi.geojson.models.CountryBounds;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class LocationService {
    private final Map<String, List<AdminRegion>> countries = new HashMap<>();
    private final Map<String, CountryBounds> countryBounds = new HashMap<>();
    private final GeometryFactory geometryFactory;
    private final ObjectMapper objectMapper;

    public LocationService(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() throws IOException {
        // loadGeojsonFiles();
        try {
            loadGeojsonFiles();
        } catch (IOException e) {
            // Nice little catch all that just loggs the error but not c the application
            // startup
            System.err.println("Warning: Failed to load some GeoJSON files: " + e.getMessage());
            countries.clear();
            countryBounds.clear();
        }

    }

    private void loadGeojsonFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/*.json");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null)
                continue;

            String countryCode = filename.split("_")[1];
            loadGeojson(countryCode, resource);
        }
    }

    private void loadGeojson(String countryCode, Resource resource) throws IOException {
        JsonNode root = objectMapper.readTree(resource.getInputStream());
        if (!"FeatureCollection".equals(root.get("type").asText())) {
            throw new IllegalArgumentException("Invalid GeoJSON: not a FeatureCollection");
        }

        List<AdminRegion> regions = new ArrayList<>();
        CountryBounds bounds = new CountryBounds();

        for (JsonNode feature : root.get("features")) {
            processFeature(feature, regions, bounds);
        }

        countries.put(countryCode, regions);
        countryBounds.put(countryCode, bounds);

        System.out.println("Loaded " + regions.size() + " regions for " + countryCode);
    }

    private void processFeature(JsonNode feature, List<AdminRegion> regions, CountryBounds bounds) {
        try {
            JsonNode properties = feature.get("properties");
            JsonNode geometry = feature.get("geometry");

            MultiPolygon multiPolygon = createMultiPolygon(geometry);
            bounds.updateBounds(multiPolygon);

            Map<String, String> adminLevels = extractAdminLevels(properties);
            Map<String, Object> originalProps = objectMapper.convertValue(properties, Map.class);

            regions.add(new AdminRegion(multiPolygon, adminLevels, originalProps));
        } catch (Exception e) {
            System.err.println("Error processing feature: " + e.getMessage());
        }
    }

    private MultiPolygon createMultiPolygon(JsonNode geometry) {
        String type = geometry.get("type").asText();
        JsonNode coordinates = geometry.get("coordinates");

        List<Polygon> polygons = new ArrayList<>();
        if ("MultiPolygon".equals(type)) {
            for (JsonNode polygonCoords : coordinates) {

                org.locationtech.jts.geom.LinearRing shell = createRing(polygonCoords.get(0));
                org.locationtech.jts.geom.LinearRing[] holes = new org.locationtech.jts.geom.LinearRing[polygonCoords
                        .size() - 1];

                for (int i = 1; i < polygonCoords.size(); i++) {
                    holes[i - 1] = createRing(polygonCoords.get(i));
                }

                polygons.add(geometryFactory.createPolygon(shell, holes));
            }
        } else if ("Polygon".equals(type)) {
            org.locationtech.jts.geom.LinearRing shell = createRing(coordinates.get(0));
            org.locationtech.jts.geom.LinearRing[] holes = new org.locationtech.jts.geom.LinearRing[coordinates.size()
                    - 1];

            for (int i = 1; i < coordinates.size(); i++) {
                holes[i - 1] = createRing(coordinates.get(i));
            }

            polygons.add(geometryFactory.createPolygon(shell, holes));
        }

        return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    private org.locationtech.jts.geom.LinearRing createRing(JsonNode coordinates) {
        org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            JsonNode point = coordinates.get(i);
            coords[i] = new org.locationtech.jts.geom.Coordinate(point.get(0).asDouble(), point.get(1).asDouble());
        }
        return geometryFactory.createLinearRing(coords);
    }

    private Polygon createPolygon(JsonNode coordinates) {
        org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            JsonNode point = coordinates.get(i);
            coords[i] = new org.locationtech.jts.geom.Coordinate(point.get(0).asDouble(), point.get(1).asDouble());
        }
        CoordinateSequence coordinateSequence = geometryFactory.getCoordinateSequenceFactory().create(coords);
        return geometryFactory.createPolygon(coordinateSequence);
    }

    private Map<String, String> extractAdminLevels(JsonNode properties) {
        Map<String, String> adminLevels = new HashMap<>();
        properties.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (key.startsWith("NAME_")) {
                String value = entry.getValue().asText();
                if (!"NA".equals(value)) {
                    adminLevels.put("level_" + key.split("_")[1], value);
                }
            } else if ("COUNTRY".equals(key)) {
                adminLevels.put("country", entry.getValue().asText());
            }
        });
        return adminLevels;
    }

    public Optional<Map<String, Object>> findLocation(double lat, double lon) {

        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(lon, lat));

        for (Map.Entry<String, List<AdminRegion>> entry : countries.entrySet()) {
            String countryCode = entry.getKey();
            CountryBounds bounds = countryBounds.get(countryCode);

            if (bounds.containsPoint(lat, lon)) {
                for (AdminRegion region : entry.getValue()) {
                    try {
                        if (region.getGeometry().contains(point)) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("country", countryCode);
                            result.put("levelsCount", region.getProperties().size());
                            result.put("properties", region.getProperties());
                            // result.put("original_properties", region.getOriginalProperties());
                            return Optional.of(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Error checking region: " + e.getMessage());
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Map<String, Map<String, Integer>> getAvailableCountries() {
        return countries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> extractCountryName(entry.getValue()),
                        entry -> getAdminLevels(entry.getValue())));
    }

    private String extractCountryName(List<AdminRegion> regions) {
        if (regions.isEmpty())
            return "Unknown";
        return regions.get(0).getProperties().getOrDefault("country", "Unknown").toString();
    }

    private Map<String, Integer> getAdminLevels(List<AdminRegion> regions) {
        if (regions.isEmpty())
            return Collections.emptyMap();

        Map<String, Integer> levelNames = new HashMap<>();
        Map<String, String> properties = regions.get(0).getProperties();

        for (String key : properties.keySet()) {
            if (key.startsWith("level_")) {
                int levelNumber = Integer.parseInt(key.split("_")[1]);
                levelNames.put(key, levelNumber);
            }
        }

        return levelNames;
    }

    // Will use this again later maybe
    private String getLevelNameFromProperties(Map<String, String> properties, int level) {
        String typeKey = "TYPE_" + level;

        Object levelName = properties.get(typeKey);
        return levelName != null ? levelName.toString() : "Unknown Level";
    }

}
