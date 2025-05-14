package com.pharmacie.gestion_pharmacie.service;

import com.pharmacie.gestion_pharmacie.model.Medication;
import com.pharmacie.gestion_pharmacie.model.Pharmacy;
import com.pharmacie.gestion_pharmacie.model.Stock;
import com.pharmacie.gestion_pharmacie.model.StockItem;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.repository.PharmacyRepository;
import com.pharmacie.gestion_pharmacie.repository.StockItemRepository;
import com.pharmacie.gestion_pharmacie.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private StockRepository stockRepository;
    
    @Mock
    private StockItemRepository stockItemRepository;
    
    @Mock
    private MedicationRepository medicationRepository;
    
    @Mock
    private PharmacyRepository pharmacyRepository;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToStock_WhenStockExists_ShouldAddToExistingStock() {
        // Arrange
        Long pharmacyId = 1L;
        Long medicationId = 1L;
        int quantity = 10;
        LocalDate expirationDate = LocalDate.now().plusMonths(6);

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(pharmacyId);

        Medication medication = new Medication();
        medication.setId(medicationId);

        Stock existingStock = new Stock();
        existingStock.setId(1L);
        existingStock.setPharmacy(pharmacy);
        existingStock.setMedication(medication);

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication));
        when(stockRepository.findByPharmacyAndMedication(pharmacy, medication))
            .thenReturn(Optional.of(existingStock));

        // Act
        stockService.addToStock(pharmacyId, medicationId, quantity, expirationDate);

        // Assert
        verify(stockItemRepository).save(any(StockItem.class));
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void addToStock_WhenStockDoesNotExist_ShouldCreateNewStock() {
        // Arrange
        Long pharmacyId = 1L;
        Long medicationId = 1L;
        int quantity = 10;
        LocalDate expirationDate = LocalDate.now().plusMonths(6);

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(pharmacyId);

        Medication medication = new Medication();
        medication.setId(medicationId);

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication));
        when(stockRepository.findByPharmacyAndMedication(pharmacy, medication))
            .thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        stockService.addToStock(pharmacyId, medicationId, quantity, expirationDate);

        // Assert
        verify(stockRepository).save(any(Stock.class));
        verify(stockItemRepository).save(any(StockItem.class));
    }
} 