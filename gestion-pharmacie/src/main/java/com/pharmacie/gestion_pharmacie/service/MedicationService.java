package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private ImageService imageService;

    public Medication addMedication(String name, String description, MultipartFile image) throws Exception {
        try {
            // Sauvegarder l'image
            String imagePath = imageService.saveImage(image);

            // Créer le médicament
            Medication medication = new Medication();
            medication.setName(name);
            medication.setDescription(description);
            medication.setImageUrl(imagePath);

            // Sauvegarder le médicament
            return medicationRepository.save(medication);
        } catch (Exception e) {
            throw new Exception("Erreur lors de l'ajout du médicament: " + e.getMessage());
        }
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }
} 