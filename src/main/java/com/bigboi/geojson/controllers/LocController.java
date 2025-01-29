package com.bigboi.geojson.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bigboi.geojson.models.Coordinate;
import com.bigboi.geojson.services.LocationService;

@RestController
@RequestMapping("/api")
public class LocController {
    private final LocationService locationService;

    public LocController(LocationService locationService) {
        this.locationService = locationService;
    }

    // record Coordinate(double latitude, double longitude) {
    // }

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
}
