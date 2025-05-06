package com.pharmacie.gestion_pharmacie.dto;

import lombok.Data;
import java.util.List;

@Data
public class MedicationStockDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer totalQuantity;
    private Integer seuil;
    private List<LotDTO> lots;

    @Data
    public static class LotDTO {
        private Long id;
        private Integer quantity;
        private String expirationDate;
    }
} 