package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private MedicationService medicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addMedication_ShouldCreateMedicationAndSaveImage() throws Exception {
        String name = "Paracétamol";
        String description = "Antidouleur";
        Long pharmacyId = 1L;
        int seuil = 10;
        double sellPrice = 5.99;

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(pharmacyId);

        Medication medication = new Medication();
        medication.setName(name);
        medication.setDescription(description);
        medication.setPharmacy(pharmacy);
        medication.setSeuil(seuil);
        medication.setSellPrice(sellPrice);

        MockMultipartFile image = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(imageService.saveImage(any(MultipartFile.class), eq(pharmacyId))).thenReturn("/images/test.jpg");
        when(medicationRepository.save(any(Medication.class))).thenReturn(medication);

        Medication result = medicationService.addMedication(name, description, image, pharmacyId, seuil, sellPrice);

        verify(pharmacyRepository).findById(pharmacyId);
        verify(imageService).saveImage(image, pharmacyId);
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void getAllMedications_ShouldReturnAllMedications() {
        List<Medication> expectedMedications = Arrays.asList(
            new Medication(),
            new Medication()
        );

        when(medicationRepository.findAll()).thenReturn(expectedMedications);

        List<Medication> result = medicationService.getAllMedications();

        verify(medicationRepository).findAll();
        assert result.size() == expectedMedications.size();
    }
} 