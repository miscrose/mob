package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.dto.PurchaseItemDTO;
import com.pharmacie.gestion_pharmacie.model.*;
import com.pharmacie.gestion_pharmacie.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPurchase_ShouldCreatePurchaseAndUpdateStock() {
        Long pharmacyId = 1L;
        Long medicationId = 1L;
        int quantity = 10;
        double unitPrice = 5.0;
        LocalDate expirationDate = LocalDate.now().plusMonths(6);

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(pharmacyId);

        Medication medication = new Medication();
        medication.setId(medicationId);

        PurchaseItemDTO itemDTO = new PurchaseItemDTO();
        itemDTO.setMedicationId(medicationId);
        itemDTO.setQuantity(quantity);
        itemDTO.setUnitPrice(unitPrice);
        itemDTO.setExpirationDate(expirationDate);

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setPharmacy(pharmacy);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setTotalAmount(quantity * unitPrice);

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseItemRepository.saveAll(any())).thenReturn(Arrays.asList(new PurchaseItem()));

        Purchase result = purchaseService.createPurchase(pharmacyId, Arrays.asList(itemDTO));

        verify(pharmacyRepository).findById(pharmacyId);
        verify(medicationRepository).findById(medicationId);
        verify(purchaseRepository).save(any(Purchase.class));
        verify(purchaseItemRepository).saveAll(any());
        verify(stockService).addToStock(pharmacyId, medicationId, quantity, expirationDate);
    }

    @Test
    void getPurchasesByPharmacy_ShouldReturnPurchases() {
        Long pharmacyId = 1L;
        List<Purchase> expectedPurchases = Arrays.asList(new Purchase(), new Purchase());

        when(purchaseRepository.findByPharmacyId(pharmacyId)).thenReturn(expectedPurchases);

        List<Purchase> result = purchaseService.getPurchasesByPharmacy(pharmacyId);

        verify(purchaseRepository).findByPharmacyId(pharmacyId);
        assert result.size() == expectedPurchases.size();
    }
} 