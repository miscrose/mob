package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.dto.PurchaseRequest;
import com.pharmacie.gestion_pharmacie.model.Purchase;
import com.pharmacie.gestion_pharmacie.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")

public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }


    @PostMapping("/test")
    public void test() {
  
        System.out.println("test");
    }




    @PostMapping("/createPurchase")
    public ResponseEntity<Purchase> createPurchase(
        @RequestBody PurchaseRequest request
    ) {
        Purchase purchase = purchaseService.createPurchase(request.getPharmacyId(), request.getItems());
        return ResponseEntity.ok(purchase);
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<Purchase>> getPurchasesByPharmacy(
        @PathVariable Long pharmacyId
    ) {
        List<Purchase> purchases = purchaseService.getPurchasesByPharmacy(pharmacyId);
        return ResponseEntity.ok(purchases);
    }
} 