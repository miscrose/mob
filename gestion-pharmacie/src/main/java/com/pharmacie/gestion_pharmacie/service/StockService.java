package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.dto.MedicationStockDTO;
import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.model.Stock;
import com.pharmacie.gestion_pharmacie.model.StockItem;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import com.pharmacie.gestion_pharmacie.repository.StockItemRepository;
import com.pharmacie.gestion_pharmacie.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockItemRepository stockItemRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;

    @Autowired
    public StockService(
        StockRepository stockRepository,
        StockItemRepository stockItemRepository,
        MedicationRepository medicationRepository,
        PharmacyRepository pharmacyRepository
    ) {
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.medicationRepository = medicationRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Transactional
    public void addToStock(Long pharmacyId, Long medicationId, int quantity, LocalDate expirationDate) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        Medication medication = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

        // Vérifier si un stock existe déjà pour cette pharmacie et ce médicament
        Optional<Stock> existingStock = stockRepository.findByPharmacyAndMedication(pharmacy, medication);
        
        Stock stock;
        if (existingStock.isPresent()) {
            stock = existingStock.get();
        } else {
            // Créer un nouveau stock si aucun n'existe
            stock = new Stock();
            stock.setPharmacy(pharmacy);
            stock.setMedication(medication);
            stock = stockRepository.save(stock);
        }

        // Créer un nouveau StockItem
        StockItem stockItem = new StockItem();
        stockItem.setStock(stock);
        stockItem.setQuantity(quantity);
        stockItem.setExpirationDate(expirationDate);
        
        stockItemRepository.save(stockItem);
    }

    public List<MedicationStockDTO> getAllMedicationsWithStock(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        List<Medication> medications = medicationRepository.findByPharmacy(pharmacy);
        
        return medications.stream().map(medication -> {
            MedicationStockDTO dto = new MedicationStockDTO();
            dto.setId(medication.getId());
            dto.setName(medication.getName());
            dto.setImageUrl(medication.getImageUrl());
            
            List<StockItem> stockItems = stockItemRepository.findByStock_Medication(medication);
            
            int totalQuantity = stockItems.stream()
                .mapToInt(StockItem::getQuantity)
                .sum();
            dto.setTotalQuantity(totalQuantity);
            
            dto.setLots(stockItems.stream().map(stockItem -> {
                MedicationStockDTO.LotDTO lotDTO = new MedicationStockDTO.LotDTO();
                lotDTO.setId(stockItem.getId());
                lotDTO.setQuantity(stockItem.getQuantity());
                lotDTO.setExpirationDate(stockItem.getExpirationDate().toString());
                return lotDTO;
            }).collect(Collectors.toList()));
            
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Stock> getStocksByPharmacy(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        return stockRepository.findByPharmacy(pharmacy);
    }
} 