package com.bigboi.geojson.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.bigboi.geojson.events.GeoJsonReloadEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeoJsonService {
    private final Map<String, Object> loadedGeoJsonData = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public GeoJsonService(ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    public void reloadGeoJsonData() {
        try {
            loadedGeoJsonData.clear();
            Path dataDir = Paths.get("data");
            if (Files.exists(dataDir)) {
                Files.list(dataDir)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(this::loadGeoJsonFile);
            }
            // Publish event that data has been reloaded
            eventPublisher.publishEvent(new GeoJsonReloadEvent(this));
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload GeoJSON data", e);
        }
    }

    private void loadGeoJsonFile(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            Object geoJson = objectMapper.readValue(filePath.toFile(), Object.class);
            loadedGeoJsonData.put(fileName, geoJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GeoJSON file: " + filePath, e);
        }
    }

    public Object getGeoJsonData(String fileName) {
        return loadedGeoJsonData.get(fileName);
    }

    public Map<String, Object> getAllGeoJsonData() {
        return loadedGeoJsonData;
    }
}
