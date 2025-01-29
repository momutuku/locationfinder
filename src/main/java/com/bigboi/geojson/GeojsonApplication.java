package com.bigboi.geojson;

import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GeojsonApplication {
	public static void main(String[] args) {
		SpringApplication.run(GeojsonApplication.class, args);

	}

	@Bean
	public GeometryFactory geometryFactory() {
		return new GeometryFactory();
	}
}