package com.pharmacie.gestion_pharmacie.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.pharmacie.gestion_pharmacie.service.DashboardService;
import com.pharmacie.gestion_pharmacie.dto.DashboardSalesDTO;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/sales/stats")
    public DashboardSalesDTO getDailySalesStats(
            @RequestParam Long pharmacyId,
            @RequestParam(defaultValue = "30") int days) {
        return dashboardService.getDailySalesStats(pharmacyId, days);
    }
} 