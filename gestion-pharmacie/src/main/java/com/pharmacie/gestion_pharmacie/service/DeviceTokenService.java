package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.DeviceToken;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.DeviceTokenRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceTokenService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Transactional
    public DeviceToken saveToken(String token, Long pharmacyId) {
        
        if (deviceTokenRepository.existsByToken(token)) {
            throw new RuntimeException("Ce token existe déjà");
        }

        
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
            .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setToken(token);
        deviceToken.setPharmacy(pharmacy);

        return deviceTokenRepository.save(deviceToken);
    }

    public List<DeviceToken> getTokensByPharmacy(Long pharmacyId) {
     
        return deviceTokenRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional
    public void deleteToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
} 