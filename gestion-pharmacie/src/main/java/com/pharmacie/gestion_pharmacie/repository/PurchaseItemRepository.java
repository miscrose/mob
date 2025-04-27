package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
} 