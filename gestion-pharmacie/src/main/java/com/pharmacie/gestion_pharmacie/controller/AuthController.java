package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import com.pharmacie.gestion_pharmacie.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> registerPharmacy(@RequestBody Pharmacy pharmacy) {
        if (pharmacyRepository.existsByEmail(pharmacy.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already taken!");
        }

        // Encode password
        pharmacy.setPassword(passwordEncoder.encode(pharmacy.getPassword()));
        
        // Save pharmacy
        pharmacyRepository.save(pharmacy);
        
        return ResponseEntity.ok("Pharmacy registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticatePharmacy(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("Tentative de connexion avec email: " + loginRequest.get("email"));
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.get("email"),
                    loginRequest.get("password")
                )
            );

            System.out.println("Authentification réussie");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Récupérer les informations de la pharmacie
            Pharmacy pharmacy = pharmacyRepository.findByEmail(loginRequest.get("email"))
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

            System.out.println("Pharmacie trouvée: " + pharmacy.getName());

            // Créer les claims pour le token
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", pharmacy.getId());
            claims.put("name", pharmacy.getName());
            claims.put("email", pharmacy.getEmail());
            claims.put("address", pharmacy.getAddress());
            claims.put("phone", pharmacy.getPhone());

            // Générer le token JWT
            String token = jwtUtil.generateToken(pharmacy.getEmail(), claims);
            System.out.println("Token généré avec succès");

            // Créer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("pharmacy", claims);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }
} 