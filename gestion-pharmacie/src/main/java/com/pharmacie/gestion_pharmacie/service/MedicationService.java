package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Medication addMedication(String name, String description, MultipartFile image, Long pharmacyId, int seuil, double sellPrice) throws Exception {
        try {
            
            Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new Exception("Pharmacie non trouvée"));

            String imagePath = imageService.saveImage(image, pharmacyId);

            Medication medication = new Medication();
            medication.setName(name);
            medication.setDescription(description);
            medication.setImageUrl(imagePath);
            medication.setPharmacy(pharmacy);
            medication.setSeuil(seuil);
            medication.setSellPrice(sellPrice);

            return medicationRepository.save(medication);
        } catch (Exception e) {
            throw new Exception("Erreur lors de l'ajout du médicament: " + e.getMessage());
        }
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }
} 