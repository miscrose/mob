package com.pharmacie.gestion_pharmacie.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
@Data
@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medication_id", nullable = false)
    private Long medicationId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

   
} 