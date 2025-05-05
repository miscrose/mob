package com.pharmacie.gestion_pharmacie.controller;

import com.pharmacie.gestion_pharmacie.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send/{pharmacyId}")
    public ResponseEntity<String> sendNotification(
            @PathVariable Long pharmacyId,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            notificationService.sendNotificationToPharmacy(pharmacyId, title, body);
            return ResponseEntity.ok("Notification envoyée avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }
} 