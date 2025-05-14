package com.pharmacie.gestion_pharmacie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    private String authToken;
    private Pharmacy testPharmacy;

    @Test
    public void testCompleteAuthFlow() throws Exception {
        try {
          
            signup();
            
            testInvalidLogin();
            login();
            
           
            
         
            testSignupWithExistingEmail();
            
        } catch (Exception e) {
            fail("Le test a échoué à l'étape: " + e.getMessage());
        }
    }

    private void signup() throws Exception {
       
        medicationRepository.deleteAll(); 
        pharmacyRepository.deleteAll();  

   
        testPharmacy = new Pharmacy();
        testPharmacy.setName("Test Pharmacy");
        testPharmacy.setEmail("test@pharmacy.com");
        testPharmacy.setPassword("password123");
        testPharmacy.setAddress("123 Test Street");
        testPharmacy.setPhone("1234567890");

     
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPharmacy)))
                .andExpect(status().isOk())
                .andExpect(content().string("Pharmacy registered successfully!"));

      
        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByEmail("test@pharmacy.com");
        if (!optionalPharmacy.isPresent()) {
            throw new Exception("Signup failed: Pharmacy not found in database");
        }

        Pharmacy savedPharmacy = optionalPharmacy.get();
        assertEquals("Test Pharmacy", savedPharmacy.getName(), "Le nom de la pharmacie ne correspond pas");
    }

    private void login() throws Exception {
      
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@pharmacy.com");
        loginRequest.put("password", "password123");

       
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.pharmacy").exists())
                .andExpect(jsonPath("$.pharmacy.email").value("test@pharmacy.com"))
                .andReturn();

       
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        authToken = jsonNode.get("token").asText();

        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Login failed: No token received");
        }
    }

    private void testInvalidLogin() throws Exception {
      
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@pharmacy.com");
        loginRequest.put("password", "wrongpassword");

     
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email ou mot de passe incorrect"));
    }

    private void testSignupWithExistingEmail() throws Exception {
       
        Pharmacy anotherPharmacy = new Pharmacy();
        anotherPharmacy.setName("Another Pharmacy");
        anotherPharmacy.setEmail("test@pharmacy.com"); 
        anotherPharmacy.setAddress("456 Another Street");
        anotherPharmacy.setPhone("9876543210");

    
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherPharmacy)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already taken!"));
    }
}