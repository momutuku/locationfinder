package com.bigboi.geojson.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bigboi.geojson.events.GeoJsonReloadEvent;

@Component
public class GeoJsonReloadListener {

    @EventListener
    public void handleGeoJsonReload(GeoJsonReloadEvent event) {

        System.out.println("GeoJSON data has been reloaded");
    }
}
