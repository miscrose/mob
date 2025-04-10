package com.pharmacie.gestion_pharmacie.security;

import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class PharmacyUserDetails implements UserDetails {
    private final Pharmacy pharmacy;

    public PharmacyUserDetails(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PHARMACY"));
    }

    @Override
    public String getPassword() {
        return pharmacy.getPassword();
    }

    @Override
    public String getUsername() {
        return pharmacy.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return pharmacy.isEnabled();
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }
} 