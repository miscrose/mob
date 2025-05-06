package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.dto.MedicationStockDTO;
import com.pharmacie.gestion_pharmacie.dto.MedicationSaleDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockItemRepository stockItemRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final NotificationService notificationService;

    @Autowired
    public StockService(
        StockRepository stockRepository,
        StockItemRepository stockItemRepository,
        MedicationRepository medicationRepository,
        PharmacyRepository pharmacyRepository,
        NotificationService notificationService
    ) {
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.medicationRepository = medicationRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void addToStock(Long pharmacyId, Long medicationId, int quantity, LocalDate expirationDate) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        Medication medication = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

        Optional<Stock> existingStock = stockRepository.findByPharmacyAndMedication(pharmacy, medication);
        
        Stock stock;
        if (existingStock.isPresent()) {
            stock = existingStock.get();
        } else {
            
            stock = new Stock();
            stock.setPharmacy(pharmacy);
            stock.setMedication(medication);
            stock = stockRepository.save(stock);
        }

      
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
            dto.setSeuil(medication.getSeuil());
            
            // Trouver le stock pour cette pharmacie et ce médicament
            Optional<Stock> stockOpt = stockRepository.findByPharmacyAndMedication(pharmacy, medication);
            
            if (stockOpt.isPresent()) {
                Stock stock = stockOpt.get();
                List<StockItem> stockItems = stockItemRepository.findByStock(stock);
            
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
            } else {
                // Si pas de stock, mettre des valeurs par défaut
                dto.setTotalQuantity(0);
                dto.setLots(new ArrayList<>());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

  

    public void verifmedicationquantity(Long pharmacyId, Long medicationId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        Medication medication = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

     
        Stock stock = stockRepository.findByPharmacyAndMedication(pharmacy, medication)
            .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

   
        List<StockItem> stockItems = stockItemRepository.findByStock(stock);
        
        int totalQuantity = stockItems.stream()
            .mapToInt(StockItem::getQuantity)
            .sum();

        if (totalQuantity < medication.getSeuil()) {
            String title = "Alerte Stock";
            String body = String.format("Le médicament %s est en dessous du seuil minimum. Quantité actuelle: %d, Seuil: %d", 
                medication.getName(), totalQuantity, medication.getSeuil());
            
            notificationService.sendNotificationToPharmacy(pharmacyId, title, body);
        }
    }

    public void verifAllMedicationQuantity(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        List<Medication> medications = medicationRepository.findByPharmacy(pharmacy);
        StringBuilder messageBuilder = new StringBuilder();
        int countBelowThreshold = 0;

        for (Medication medication : medications) {
            Optional<Stock> stockOpt = stockRepository.findByPharmacyAndMedication(pharmacy, medication);
            
            if (stockOpt.isPresent()) {
                Stock stock = stockOpt.get();
                List<StockItem> stockItems = stockItemRepository.findByStock(stock);
                int totalQuantity = stockItems.stream()
                    .mapToInt(StockItem::getQuantity)
                    .sum();

                if (totalQuantity < medication.getSeuil()) {
                    countBelowThreshold++;
                    messageBuilder.append(String.format("\n- %s: Quantité actuelle: %d, Seuil: %d", 
                        medication.getName(), totalQuantity, medication.getSeuil()));
                }
            }
        }

        if (countBelowThreshold > 0) {
            String title = "Alerte Stock Global";
            String body = String.format("Il y a %d médicaments en dessous du seuil minimum:%s", 
                countBelowThreshold, messageBuilder.toString());
            
            notificationService.sendNotificationToPharmacy(pharmacyId, title, body);
        }
    }

    @Transactional
    public void removeFromStock(Long pharmacyId, Long medicationId, int quantity) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        Medication medication = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

       
        Stock stock = stockRepository.findByPharmacyAndMedication(pharmacy, medication)
            .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

     
        List<StockItem> stockItems = stockItemRepository.findByStock(stock);
        
     
        int totalQuantity = stockItems.stream()
            .mapToInt(StockItem::getQuantity)
            .sum();

     
        if (totalQuantity < quantity) {
            throw new RuntimeException("Quantité insuffisante en stock. Quantité disponible: " + totalQuantity);
        }

           int remainingQuantity = quantity;
        for (StockItem stockItem : stockItems) {
            if (remainingQuantity <= 0) break;

            int currentQuantity = stockItem.getQuantity();
            if (currentQuantity >= remainingQuantity) {
                stockItem.setQuantity(currentQuantity - remainingQuantity);
                remainingQuantity = 0;
            } else {
                stockItem.setQuantity(0);
                remainingQuantity -= currentQuantity;
            }
            stockItemRepository.save(stockItem);
        }

      
        verifmedicationquantity(pharmacyId, medicationId);
    }

    public List<MedicationSaleDTO> getAllMedicationsForSale(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
            
        List<Medication> medications = medicationRepository.findByPharmacy(pharmacy);
        
        return medications.stream().map(medication -> {
            MedicationSaleDTO dto = new MedicationSaleDTO();
            dto.setId(medication.getId());
            dto.setName(medication.getName());
            dto.setImageUrl(medication.getImageUrl());
            dto.setSellPrice(medication.getSellPrice());
            
            // Trouver le stock pour cette pharmacie et ce médicament
            Optional<Stock> stockOpt = stockRepository.findByPharmacyAndMedication(pharmacy, medication);
            
            if (stockOpt.isPresent()) {
                Stock stock = stockOpt.get();
                List<StockItem> stockItems = stockItemRepository.findByStock(stock);
                
                int totalQuantity = stockItems.stream()
                    .mapToInt(StockItem::getQuantity)
                    .sum();
                dto.setTotalQuantity(totalQuantity);
            } else {
                dto.setTotalQuantity(0);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

} 