package com.pharmacie.gestion_pharmacie.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
    private final String baseUploadDir = "uploads/medications/";

    public String saveImage(MultipartFile file, Long pharmacyId) throws IOException {
      
        String pharmacyDir = baseUploadDir + "pharmacy_" + pharmacyId + "/";
        Path uploadPath = Paths.get(pharmacyDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

     
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Le nom du fichier ne peut pas être null");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

     
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

      
        return pharmacyDir + newFilename;
    }
}