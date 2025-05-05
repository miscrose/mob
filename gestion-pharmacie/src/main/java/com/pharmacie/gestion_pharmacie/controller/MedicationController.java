package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;



    @PostMapping("/test")
    public void test() {
        System.out.println("test");
    }

    @PostMapping("/add")
    public ResponseEntity<?> addMedication(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image,
            @RequestParam("pharmacyId") long pharmacyId,
            @RequestParam("seuil") int seuil) {
        try {
            System.out.println("ID de la pharmacie reçu : " + pharmacyId);
            Medication medication = medicationService.addMedication(name, description, image, pharmacyId, seuil);
            return ResponseEntity.ok(medication);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout du médicament: " + e.getMessage());
        }
    }

    @GetMapping("allmedications")
    public ResponseEntity<?> getAllMedications() {
        try {
            return ResponseEntity.ok(medicationService.getAllMedications());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des médicaments: " + e.getMessage());
        }
    }
} 