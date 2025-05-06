package com.pharmacie.gestion_pharmacie.dto;

import lombok.Data;

@Data
public class MedicationSaleDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer totalQuantity;
    private Double sellPrice;
} 