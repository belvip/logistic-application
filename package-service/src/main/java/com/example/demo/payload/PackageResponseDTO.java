package com.example.demo.payload;

import com.example.demo.model.PackageStatus;

public record PackageResponseDTO(
        Long packageId,
        String description,
        Double weight,
        Boolean fragile,
        PackageStatus status
) {}
