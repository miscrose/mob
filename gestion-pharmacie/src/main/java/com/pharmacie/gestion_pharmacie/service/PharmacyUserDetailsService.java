package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import com.pharmacie.gestion_pharmacie.security.PharmacyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PharmacyUserDetailsService implements UserDetailsService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Pharmacy pharmacy = pharmacyRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pharmacy not found with email: " + email));
        
        return new PharmacyUserDetails(pharmacy);
    }
} 