package org.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ImageService {

    private final JdbcTemplate jdbcTemplate;
    private final String imageFolder;

    public ImageService(JdbcTemplate jdbcTemplate, @Value("${app.image.folder}") String imageFolder) {
        this.jdbcTemplate = jdbcTemplate;
        this.imageFolder = imageFolder;
    }

    @SuppressWarnings({"UseSpecificCatch","unused"})

    public void processImages() {
        File folder = new File(imageFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("⚠️ Image folder not found!");
            return;
        }

        try (var paths = Files.list(Paths.get(imageFolder))) {
            List<String> imageFiles = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList(); // Java 16+

            for (String image : imageFiles) {
                if (image.toLowerCase().matches(".*\\.(jpg|jpeg|png)")) {
                    String formattedName = image.replace("_", " ")
                            .replaceAll("\\.[^.]+$", "")
                            .toLowerCase();
                    String updateQuery = "UPDATE books SET image = ? WHERE LOWER(title) = ?";
                    jdbcTemplate.update(updateQuery, image, formattedName);
                }
            }

            System.out.println("✅ Database image names updated successfully!");
        } catch (Exception e) {
            System.out.println("⚠️ Error processing images: " + e.getMessage());
        }
    }
}
