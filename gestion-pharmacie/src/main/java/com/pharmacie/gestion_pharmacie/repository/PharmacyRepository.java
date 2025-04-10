package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByEmail(String email);
    boolean existsByEmail(String email);
} 