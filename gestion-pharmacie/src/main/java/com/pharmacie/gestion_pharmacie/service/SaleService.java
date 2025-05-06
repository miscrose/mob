package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.dto.SaleItem;
import com.pharmacie.gestion_pharmacie.model.Sale;
import com.pharmacie.gestion_pharmacie.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final StockService stockService;

    @Autowired
    public SaleService(SaleRepository saleRepository, StockService stockService) {
        this.saleRepository = saleRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void createSale(Long pharmacyId, List<SaleItem> items) {
        for (SaleItem item : items) {
            // Vérifier et retirer du stock
            stockService.removeFromStock(pharmacyId, item.getMedicationId(), item.getQuantity());

            // Créer l'enregistrement de vente
            Sale sale = new Sale();
            sale.setMedicationId(item.getMedicationId());
            sale.setQuantity(item.getQuantity());
            sale.setPharmacyId(pharmacyId);
            sale.setSaleDate(LocalDateTime.now());

            saleRepository.save(sale);
        }
    }

    public List<Sale> getSalesByPharmacy(Long pharmacyId) {
        return saleRepository.findByPharmacyId(pharmacyId);
    }
} 