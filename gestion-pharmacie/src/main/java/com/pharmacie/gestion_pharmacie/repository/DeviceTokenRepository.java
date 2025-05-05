package com.pharmacie.gestion_pharmacie.repository;

import com.pharmacie.gestion_pharmacie.model.DeviceToken;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByPharmacyId(long pharmacyid);
    boolean existsByToken(String token);
    void deleteByToken(String token);
} 