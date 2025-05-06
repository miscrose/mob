package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByPharmacyId(Long pharmacyId);
} 