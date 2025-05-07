package com.pharmacie.gestion_pharmacie.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardSalesDTO {
    private List<DailySales> dailySales;
    private double totalRevenue;
    private int totalSales;

    @Data
    public static class DailySales {
        private String date;
        private int numberOfSales;
        private double revenue;
        private Map<String, Integer> topSellingMedications; // Map<medicationName, quantity>
    }
} 