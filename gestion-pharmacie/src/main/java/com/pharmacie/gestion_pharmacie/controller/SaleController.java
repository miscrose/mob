package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.dto.MedicationSaleDTO;
import com.pharmacie.gestion_pharmacie.dto.SaleItem;
import com.pharmacie.gestion_pharmacie.service.SaleService;
import com.pharmacie.gestion_pharmacie.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    @Autowired
    private StockService stockService;

    @Autowired
    private SaleService saleService;

    @GetMapping("/medications/{pharmacyId}")
    public ResponseEntity<List<MedicationSaleDTO>> getMedicationsForSale(@PathVariable Long pharmacyId) {
        try {
            List<MedicationSaleDTO> medications = stockService.getAllMedicationsForSale(pharmacyId);
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create/{pharmacyId}")
    public ResponseEntity<?> createSale(@PathVariable Long pharmacyId, @RequestBody List<SaleItem> items) {
        try {
            saleService.createSale(pharmacyId, items);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 