package com.example.demo.service;

import com.example.demo.payload.PackageRequestDTO;
import com.example.demo.payload.PackageResponse;
import com.example.demo.payload.PackageResponseDTO;

public interface PackageService {
    PackageResponseDTO createPackage(PackageRequestDTO request);
    PackageResponse getAllPackages(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PackageResponseDTO getPackageById(Long id);

    PackageResponseDTO updatePackage(Long id, PackageRequestDTO pkgDTO);

    PackageResponseDTO deletePackage(Long id);

}