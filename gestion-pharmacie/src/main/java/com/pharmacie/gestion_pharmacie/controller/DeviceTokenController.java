package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.model.DeviceToken;
import com.pharmacie.gestion_pharmacie.service.DeviceTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    @Autowired
    private DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<DeviceToken> saveToken(
            @RequestParam String token,
            @RequestParam Long pharmacyId) {
        try {
            String decodedToken = URLDecoder.decode(token, "UTF-8");
            DeviceToken savedToken = deviceTokenService.saveToken(decodedToken, pharmacyId);
            return ResponseEntity.ok(savedToken);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<DeviceToken>> getTokensByPharmacy(@PathVariable Long pharmacyId) {
        try {
            List<DeviceToken> tokens = deviceTokenService.getTokensByPharmacy(pharmacyId);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteToken(@PathVariable String token) {
        try {
            System.out.println("Token reçu (encodé): " + token);
            String decodedToken = URLDecoder.decode(token, StandardCharsets.UTF_8.name());
            System.out.println("Token décodé: " + decodedToken);
            deviceTokenService.deleteToken(decodedToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
} 