package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.dto.MedicationStockDTO;
import com.pharmacie.gestion_pharmacie.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<MedicationStockDTO>> getAllMedications(@PathVariable Long pharmacyId) {
        try {
            List<MedicationStockDTO> medications = stockService.getAllMedicationsWithStock(pharmacyId);
            System.out.println(medications);
            System.out.println("----------------test------------");
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 