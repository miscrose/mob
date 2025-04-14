package com.pharmacie.gestion_pharmacie.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseRequest {
    private Long pharmacyId;
    private List<PurchaseItemDTO> items;
} 