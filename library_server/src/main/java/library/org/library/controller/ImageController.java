package org.library.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    // Store images in the static images directory so they are accessible via /images/
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    // ✅ Upload Image API
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // ✅ Validate file type
            if (!isValidImage(file)) {
                return ResponseEntity.badRequest().body("Invalid file type! Only JPG, PNG, and JPEG are allowed.");
            }

            // ✅ Validate file size (e.g., max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size exceeds the maximum limit of 5MB.");
            }

            // ✅ Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ✅ Generate a unique filename
            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

            // ✅ Save the file
            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            // ✅ Return image URL (relative to the base URL)
            String imageUrl = "/images/" + uniqueFileName;
            return ResponseEntity.ok().body("✅ Image uploaded successfully: " + imageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to upload image due to an internal error.");
        }
    }

    // ✅ Image Validation (basic file type validation)
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg")
        );
    }
}
