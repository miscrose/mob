package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final ImageService imageService;

    public MedicationService(
        MedicationRepository medicationRepository,
        PharmacyRepository pharmacyRepository,
        ImageService imageService
    ) {
        this.medicationRepository = medicationRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.imageService = imageService;
    }

    public Medication addMedication(String name, String description, MultipartFile image, Long pharmacyId) throws Exception {
        try {
            // Récupérer la pharmacie
            Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new Exception("Pharmacie non trouvée"));

            // Sauvegarder l'image dans le dossier de la pharmacie
            String imagePath = imageService.saveImage(image, pharmacyId);

            // Créer le médicament
            Medication medication = new Medication();
            medication.setName(name);
            medication.setDescription(description);
            medication.setImageUrl(imagePath);
            medication.setPharmacy(pharmacy);

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