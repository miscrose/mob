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
    private Pharmacy testPharmacy;

    @Test
    public void testCompleteMedicationFlow() throws Exception {
        try {
            // 1. Test création d'une pharmacie et authentification
            setupPharmacyAndAuthenticate();
            
            // 2. Test ajout d'un médicament
            testAddMedication();
            
            // 3. Test ajout d'un médicament sans image
            testAddMedicationWithoutImage();
            
            // 4. Test ajout d'un médicament avec données invalides
            testAddMedicationWithInvalidData();
            
        } catch (Exception e) {
            fail("Le test a échoué: " + e.getMessage());
        }
    }

    private void setupPharmacyAndAuthenticate() throws Exception {
        // Nettoyer la base avant le test
        medicationRepository.deleteAll();
        pharmacyRepository.deleteAll();

        // Créer une pharmacie de test
        testPharmacy = new Pharmacy();
        testPharmacy.setName("Test Pharmacy");
        testPharmacy.setEmail("test@pharmacy.com");
        testPharmacy.setPassword("password123");
        testPharmacy.setAddress("123 Test Street");
        testPharmacy.setPhone("1234567890");

        // Enregistrer la pharmacie
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPharmacy)))
                .andExpect(status().isOk())
                .andReturn();

        // Récupérer la pharmacie sauvegardée
        testPharmacy = pharmacyRepository.findByEmail("test@pharmacy.com")
                .orElseThrow(() -> new Exception("Pharmacy not found after signup"));

        // Authentifier la pharmacie
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", testPharmacy.getEmail());
        loginRequest.put("password", "password123");

        result = mockMvc.perform(post("/api/auth/login")
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
    }

    private void testAddMedication() throws Exception {
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
                .param("pharmacyId", String.valueOf(testPharmacy.getId()))
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
        assertEquals(testPharmacy.getId(), savedMedication.getPharmacy().getId());
    }

    private void testAddMedicationWithoutImage() throws Exception {
        // Créer un fichier image vide mais valide
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "empty-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            new byte[0]
        );

        mockMvc.perform(multipart("/api/medications/add")
                .file(imageFile)
                .param("name", "Test Medication No Image")
                .param("description", "Test Description")
                .param("pharmacyId", String.valueOf(testPharmacy.getId()))
                .param("seuil", "10")
                .param("sellPrice", "19.99")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Medication No Image"))
                .andReturn();
    }

    private void testAddMedicationWithInvalidData() throws Exception {
        mockMvc.perform(multipart("/api/medications/add")
                .param("name", "") // Nom vide
                .param("description", "Test Description")
                .param("pharmacyId", String.valueOf(testPharmacy.getId()))
                .param("seuil", "-1") // Seuil invalide
                .param("sellPrice", "-10.0") // Prix invalide
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
} 