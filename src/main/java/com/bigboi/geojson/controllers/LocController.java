package com.bigboi.geojson.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bigboi.geojson.models.Coordinate;
import com.bigboi.geojson.services.GeoDataDownloadService;
import com.bigboi.geojson.services.GeoJsonService;
import com.bigboi.geojson.services.LocationService;

@RestController
@RequestMapping("/api")
public class LocController {
    private final LocationService locationService;
    private final GeoDataDownloadService geoDataDownloadService;
    private final GeoJsonService geoJsonService;

    public LocController(LocationService locationService, GeoDataDownloadService geoDataDownloadService,
            GeoJsonService geoJsonService) {
        this.locationService = locationService;
        this.geoDataDownloadService = geoDataDownloadService;
        this.geoJsonService = geoJsonService;
    }

    @PostMapping("/location/find")
    public Map<String, Object> findLocation(@RequestBody Coordinate coordinate) {
        return locationService.findLocation(coordinate.getLatitude(), coordinate.getLongitude())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Location not found in any loaded country."));
    }

    @GetMapping("/countries")
    public Map<String, Map<String, Integer>> getCountries() {
        return locationService.getAvailableCountries();
    }

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchData() {
        try {
            geoDataDownloadService.fetchData();
            return ResponseEntity.ok("Data fetched successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error fetching data: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGeoJsonData() {
        return ResponseEntity.ok(geoJsonService.getAllGeoJsonData());
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Object> getGeoJsonData(@PathVariable String fileName) {
        Object data = geoJsonService.getGeoJsonData(fileName);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadGeoJsonData() {
        try {
            geoJsonService.reloadGeoJsonData();
            return ResponseEntity.ok("GeoJSON data reloaded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error reloading GeoJSON data: " + e.getMessage());
        }
    }

}
