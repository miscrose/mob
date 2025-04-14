package com.pharmacie.gestion_pharmacie.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PurchaseItemDTO {
    private Long medicationId;
    private Integer quantity;
    private Double unitPrice;
    private LocalDate expirationDate;
} 