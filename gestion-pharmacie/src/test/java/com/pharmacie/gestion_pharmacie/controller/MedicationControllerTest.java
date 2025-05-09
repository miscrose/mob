package com.pharmacie.gestion_pharmacie.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    private String authToken;

    @Test
    public void testAddMedication() throws Exception {
        try {
          
            Pharmacy pharmacy = createTestPharmacy();
            authenticate(pharmacy);
            
          
            MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
            );

          
           mockMvc.perform(multipart("/api/medications/add")
                    .file(imageFile)
                    .param("name", "Test Medication")
                    .param("description", "Test Description")
                    .param("pharmacyId", String.valueOf(pharmacy.getId()))
                    .param("seuil", "10")
                    .param("sellPrice", "19.99")
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Test Medication"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.seuil").value(10))
                    .andExpect(jsonPath("$.sellPrice").value(19.99))
                    .andReturn();

        
        
            Medication savedMedication = medicationRepository.findByName("Test Medication")
                    .orElseThrow(() -> new Exception("Medication not found in database"));
            
            assertEquals("Test Medication", savedMedication.getName());
            assertEquals("Test Description", savedMedication.getDescription());
            assertEquals(10, savedMedication.getSeuil());
            assertEquals(19.99, savedMedication.getSellPrice());
            assertEquals(pharmacy.getId(), savedMedication.getPharmacy().getId());

        } catch (Exception e) {
            fail("Le test a échoué: " + e.getMessage());
        }
    }

    private void authenticate(Pharmacy pharmacy) throws Exception {
   
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", pharmacy.getEmail());
        loginRequest.put("password", "password123"); // Utiliser le mot de passe en clair

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

      
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        authToken = jsonNode.get("token").asText();

        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Authentication failed: No token received");
        }

        System.out.println("Token received: " + authToken);
    }

    private Pharmacy createTestPharmacy() throws Exception {
        // Nettoyer la base avant le test
   //     medicationRepository.deleteAll(); 
   //     pharmacyRepository.deleteAll();   

        // Créer une pharmacie de test
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName("Test Pharmacy");
        pharmacy.setEmail("test@pharmacy.com");
        pharmacy.setPassword("password123");
        pharmacy.setAddress("123 Test Street");
        pharmacy.setPhone("1234567890");

        System.out.println("Signup request: " + objectMapper.writeValueAsString(pharmacy));

       
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pharmacy)))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("Signup response: " + result.getResponse().getContentAsString());

      
        Pharmacy savedPharmacy = pharmacyRepository.findByEmail("test@pharmacy.com")
                .orElseThrow(() -> new Exception("Pharmacy not found after signup"));

        System.out.println("Saved pharmacy: " + objectMapper.writeValueAsString(savedPharmacy));

        return savedPharmacy;
    }
} 