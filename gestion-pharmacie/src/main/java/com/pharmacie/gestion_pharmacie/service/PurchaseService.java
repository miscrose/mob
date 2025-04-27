package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.dto.PurchaseItemDTO;
import com.pharmacie.gestion_pharmacie.model.*;
import com.pharmacie.gestion_pharmacie.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final StockService stockService;

    public PurchaseService(
        PurchaseRepository purchaseRepository,
        PurchaseItemRepository purchaseItemRepository,
        MedicationRepository medicationRepository,
        PharmacyRepository pharmacyRepository,
        StockService stockService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.medicationRepository = medicationRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.stockService = stockService;
    }

    @Transactional
    public Purchase createPurchase(Long pharmacyId, List<PurchaseItemDTO> items) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

        Purchase purchase = new Purchase();
        purchase.setPharmacy(pharmacy);
        purchase.setPurchaseDate(LocalDateTime.now());
        
        // Calculer le total
        double totalAmount = items.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
        purchase.setTotalAmount(totalAmount);

        // Sauvegarder l'achat
        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Convertir les DTOs en entités et les sauvegarder
        List<PurchaseItem> purchaseItems = items.stream()
            .map(dto -> {
                Medication medication = medicationRepository.findById(dto.getMedicationId())
                    .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

                // Mettre à jour le stock
                stockService.addToStock(pharmacyId, dto.getMedicationId(), dto.getQuantity(), dto.getExpirationDate());

                PurchaseItem item = new PurchaseItem();
                item.setPurchase(savedPurchase);
                item.setMedication(medication);
                item.setQuantity(dto.getQuantity());
                item.setUnitPrice(dto.getUnitPrice());
                item.setExpirationDate(dto.getExpirationDate());
                item.setTotalPrice(dto.getQuantity() * dto.getUnitPrice());
                return item;
            })
            .collect(Collectors.toList());

        // Sauvegarder tous les items
        purchaseItemRepository.saveAll(purchaseItems);

        return savedPurchase;
    }

    public List<Purchase> getPurchasesByPharmacy(Long pharmacyId) {
        return purchaseRepository.findByPharmacyId(pharmacyId);
    }
} 