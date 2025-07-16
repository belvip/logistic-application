package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "packages")
@Data
@NoArgsConstructor
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    private String description;
    private Double weight;
    private Boolean fragile;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;
}

