package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByPharmacyId(Long pharmacyId);
} 