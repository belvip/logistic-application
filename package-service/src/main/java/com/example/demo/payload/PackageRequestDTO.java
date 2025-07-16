package com.example.demo.payload;

import com.example.demo.model.PackageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;


public record PackageRequestDTO(
        @NotBlank @Size(min = 4, max = 255) String description,
        @NotNull @Positive Double weight,
        @NotNull Boolean fragile,
        @NotNull PackageStatus status
) {}