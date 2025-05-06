package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledStockVerificationService {

    private final StockService stockService;
    private final PharmacyRepository pharmacyRepository;

    @Autowired
    public ScheduledStockVerificationService(StockService stockService, PharmacyRepository pharmacyRepository) {
        this.stockService = stockService;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Scheduled(cron = "0 7 15 * * ?")
    public void checkAllPharmaciesStock() {
        System.out.println("Démarrage de la vérification planifiée du stock...");
        
        try {
            List<Pharmacy> pharmacies = pharmacyRepository.findAll();

            
            for (Pharmacy pharmacy : pharmacies) {
                try {
                    stockService.verifAllMedicationQuantity(pharmacy.getId());
                } catch (Exception e) {
                    System.out.println("Erreur lors de la vérification du stock pour la pharmacie " + pharmacy.getName() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Vérification planifiée du stock terminée.");
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification planifiée du stock : " + e.getMessage());
        }
    }
} 