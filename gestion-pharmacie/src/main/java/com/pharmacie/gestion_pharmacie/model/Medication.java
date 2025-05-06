package com.pharmacie.gestion_pharmacie.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "medications")
@Data
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int seuil; 

    @Column(nullable = false)
    private double sellPrice;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;
} 