package com.example.demo.controller;

import com.example.demo.payload.PackageRequestDTO;
import com.example.demo.payload.PackageResponse;
import com.example.demo.payload.PackageResponseDTO;
import com.example.demo.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Package Management", description = "APIs for managing packages in the logistics system")
@RestController
@RequestMapping("${api.prefix}/packages")
@RequiredArgsConstructor
public class PackageController {
    private final PackageService packageService;

    @Operation(
            summary = "Create a new package",
            description = "Creates a new package in the system with the provided details"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Package successfully created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    @ApiResponse(responseCode = "409", description = "Package with the same description already exists", content = @Content)
    @PostMapping("/create")
    public ResponseEntity<PackageResponseDTO> createPackage(
            @Valid @RequestBody PackageRequestDTO request) {
        var response = packageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get paginated packages",
            description = "Retrieve a paginated list of packages, with sorting options"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    @GetMapping("/all")
    public ResponseEntity<PackageResponse> getAllPackages(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,

            @Parameter(description = "Page size", example = "5")
            @RequestParam(value = "pageSize", required = false) Integer pageSize,

            @Parameter(description = "Field to sort by", example = "packageId")
            @RequestParam(value = "sortBy", required = false) String sortBy,

            @Parameter(description = "Sort direction: asc or desc", example = "asc",
                    schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) {
        PackageResponse response = packageService.getAllPackages(
                pageNumber, pageSize, sortBy, sortOrder
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package by ID", description = "Retrieve a single package by its ID")
    @ApiResponse(responseCode = "200", description = "Found the package")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageResponseDTO> getPackageById(
            @Parameter(description = "ID of the package to retrieve", required = true, example = "1")
            @PathVariable Long id
    ) {
        PackageResponseDTO dto = packageService.getPackageById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update package", description = "Update all fields of an existing package by ID (not allowed if already delivered)")
    @ApiResponse(responseCode = "200", description = "Package updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid parameters or cannot update delivered package")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageResponseDTO> updatePackage(
            @Parameter(description = "ID of the package to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PackageRequestDTO pkgDTO
    ) {
        PackageResponseDTO response = packageService.updatePackage(id, pkgDTO);
        return ResponseEntity.ok(response);
    }






}
