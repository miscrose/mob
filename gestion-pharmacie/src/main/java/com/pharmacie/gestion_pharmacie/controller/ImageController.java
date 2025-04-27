package com.pharmacie.gestion_pharmacie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final Path uploadsPath = Paths.get("").toAbsolutePath().resolve("uploads");

    @GetMapping("/medications/{pharmacyId}/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String pharmacyId,
            @PathVariable String filename) throws IOException {
        
        logger.info("Tentative d'accès à l'image: pharmacyId={}, filename={}", pharmacyId, filename);
        
        Path filePath = uploadsPath.resolve("medications")
                                 .resolve("pharmacy_" + pharmacyId)
                                 .resolve(filename);
        
        logger.info("Chemin complet de l'image: {}", filePath);
        
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists()) {
            logger.info("Image trouvée: {}", filePath);
            if (resource.isReadable()) {
                logger.info("Image lisible: {}", filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                logger.error("Image non lisible: {}", filePath);
                return ResponseEntity.status(403).build();
            }
        } else {
            logger.error("Image non trouvée: {}", filePath);
            return ResponseEntity.notFound().build();
        }
    }
} 