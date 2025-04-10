package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
} 