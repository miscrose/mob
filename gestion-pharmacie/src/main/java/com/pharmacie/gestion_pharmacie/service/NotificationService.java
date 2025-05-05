package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.DeviceToken;
import com.pharmacie.gestion_pharmacie.repository.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final RestTemplate restTemplate;
    private static final String EXPO_API_URL = "https://exp.host/--/api/v2/push/send";

    @Autowired
    public NotificationService(DeviceTokenRepository deviceTokenRepository, RestTemplate restTemplate) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.restTemplate = restTemplate;
    }

    public void sendNotificationToPharmacy(Long pharmacyId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository.findByPharmacyId(pharmacyId);
        
        if (tokens.isEmpty()) {
            return;
        }

        List<String> tokenList = tokens.stream()
                .map(DeviceToken::getToken)
                .toList();
       
        Map<String, Object> notification = new HashMap<>();
        notification.put("to", tokenList);
        notification.put("title", title);
        notification.put("body", body);
        notification.put("sound", "default");
        System.out.println(notification);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(notification, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    EXPO_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Erreur lors de l'envoi de la notification. Code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de la notification", e);
        }
    }
}
