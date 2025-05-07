package com.pharmacie.gestion_pharmacie.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.pharmacie.gestion_pharmacie.repository.SaleRepository;
import com.pharmacie.gestion_pharmacie.repository.MedicationRepository;
import com.pharmacie.gestion_pharmacie.dto.DashboardSalesDTO;
import com.pharmacie.gestion_pharmacie.model.Sale;
import com.pharmacie.gestion_pharmacie.model.Medication;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private MedicationRepository medicationRepository;

    public DashboardSalesDTO getDailySalesStats(Long pharmacyId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Sale> sales = saleRepository.findByPharmacyId(pharmacyId);
        
        // Filtrer les ventes des derniers X jours
        sales = sales.stream()
            .filter(sale -> sale.getSaleDate().isAfter(startDate))
            .collect(Collectors.toList());

        DashboardSalesDTO result = new DashboardSalesDTO();
        List<DashboardSalesDTO.DailySales> dailySalesList = new ArrayList<>();
        
        // Grouper les ventes par jour
        Map<LocalDate, List<Sale>> salesByDay = sales.stream()
            .collect(Collectors.groupingBy(sale -> sale.getSaleDate().toLocalDate()));

        // Calculer les statistiques pour chaque jour
        for (Map.Entry<LocalDate, List<Sale>> entry : salesByDay.entrySet()) {
            DashboardSalesDTO.DailySales dailySales = new DashboardSalesDTO.DailySales();
            dailySales.setDate(entry.getKey().toString());
            dailySales.setNumberOfSales(entry.getValue().size());
            
            // Calculer le revenu du jour
            double dailyRevenue = entry.getValue().stream()
                .mapToDouble(sale -> {
                    Medication medication = medicationRepository.findById(sale.getMedicationId()).orElse(null);
                    return medication != null ? medication.getSellPrice() * sale.getQuantity() : 0;
                })
                .sum();
            dailySales.setRevenue(dailyRevenue);

            // Calculer les médicaments les plus vendus du jour
            Map<String, Integer> topMedications = entry.getValue().stream()
                .collect(Collectors.groupingBy(
                    sale -> {
                        Medication medication = medicationRepository.findById(sale.getMedicationId()).orElse(null);
                        return medication != null ? medication.getName() : "Unknown";
                    },
                    Collectors.summingInt(Sale::getQuantity)
                ));

            // Trier et limiter aux 3 médicaments les plus vendus
            Map<String, Integer> top3Medications = topMedications.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

            dailySales.setTopSellingMedications(top3Medications);
            
            dailySalesList.add(dailySales);
        }

        // Trier les ventes quotidiennes par date
        dailySalesList.sort(Comparator.comparing(DashboardSalesDTO.DailySales::getDate));
        result.setDailySales(dailySalesList);

        // Calculer les totaux
        result.setTotalSales(sales.size());
        result.setTotalRevenue(dailySalesList.stream()
            .mapToDouble(DashboardSalesDTO.DailySales::getRevenue)
            .sum());

        return result;
    }
} 