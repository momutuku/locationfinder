package com.bigboi.geojson.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class GeoDataDownloadService {
    private static final String BASE_URL = "https://geodata.ucdavis.edu/gadm/gadm4.1/json/";
    private final GeoJsonService geoJsonService;
    private final Path resourcesPath;

    public GeoDataDownloadService(GeoJsonService geoJsonService) throws IOException {
        this.geoJsonService = geoJsonService;
        this.resourcesPath = resolveResourcesPath();
    }

    private Path resolveResourcesPath() throws IOException {
        try {

            File dataDir = new File("src/main/resources/data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            return dataDir.toPath();
        } catch (Exception e) {

            Path path = Paths.get("data");
            Files.createDirectories(path);
            return path;
        }
    }

    public void fetchData() {
        try {

            Files.createDirectories(resourcesPath);

            Map<String, Map<Integer, String>> countryFiles = getCountryFiles();

            // Download files
            downloadFiles(countryFiles);

            geoJsonService.reloadGeoJsonData();

        } catch (IOException e) {
            throw new RuntimeException("Error fetching geodata: " + e.getMessage(), e);
        }
    }

    private Map<String, Map<Integer, String>> getCountryFiles() throws IOException {
        Map<String, Map<Integer, String>> countryFiles = new HashMap<>();

        Document doc = Jsoup.connect(BASE_URL).get();
        Elements links = doc.select("a[href$=.json]");

        for (Element link : links) {
            String href = link.attr("href");
            String filename = href.substring(href.lastIndexOf('/') + 1);
            String[] parts = filename.split("_");

            if (parts.length >= 3) {
                String countryCode = parts[1];
                int level = Integer.parseInt(parts[2].split("\\.")[0]);

                countryFiles.computeIfAbsent(countryCode, k -> new TreeMap<>())
                        .put(level, href);
            }
        }

        return countryFiles;
    }

    private void downloadFiles(Map<String, Map<Integer, String>> countryFiles) {
        System.out.println("Downloading files to: " + resourcesPath.toAbsolutePath());

        countryFiles.forEach((countryCode, files) -> {
            Integer highestLevel = files.keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(null);

            if (highestLevel != null) {
                String fileUrl = BASE_URL + files.get(highestLevel);
                String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                Path filePath = resourcesPath.resolve(fileName);

                if (Files.exists(filePath)) {
                    System.out.println("Skipping " + fileName + " - already exists");
                    return;
                }

                try {
                    System.out.println("Downloading " + fileUrl);
                    downloadFile(fileUrl, filePath);
                    System.out.println("Downloaded " + fileName + " for country " +
                            countryCode + " (Level " + highestLevel + ")");
                } catch (IOException e) {
                    System.err.println("Error downloading " + fileName + ": " + e.getMessage());
                }
            }
        });
    }

    private void downloadFile(String fileUrl, Path filePath) throws IOException {
        try (InputStream in = new URL(fileUrl).openStream();
                OutputStream out = Files.newOutputStream(filePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
