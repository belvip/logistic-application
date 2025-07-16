package com.example.demo.service;

import com.example.demo.exceptions.APIException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.PackageMapper;
import com.example.demo.model.PackageEntity;
import com.example.demo.model.PackageStatus;
import com.example.demo.payload.PackageRequestDTO;
import com.example.demo.payload.PackageResponse;
import com.example.demo.payload.PackageResponseDTO;
import com.example.demo.repository.PackageRepository;
import com.example.demo.service.impl.PackageServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    private PackageRepository pkgRepo;

    @Mock
    private PackageMapper pkgMapper;

    @InjectMocks
    private PackageServiceImpl service;

    private PackageRequestDTO validRequest;
    private PackageEntity pkgEntity;
    private PackageEntity savedEntity;

    @BeforeEach
    void setup() {
        validRequest = new PackageRequestDTO(
            "Test description",
            10.0,
            false,
            PackageStatus.PENDING
        );

        pkgEntity = new PackageEntity();
        pkgEntity.setDescription(validRequest.description());
        pkgEntity.setWeight(validRequest.weight());
        pkgEntity.setFragile(validRequest.fragile());
        pkgEntity.setStatus(validRequest.status());

        savedEntity = new PackageEntity();
        savedEntity.setPackageId(1L);
        savedEntity.setDescription(validRequest.description());
        savedEntity.setWeight(validRequest.weight());
        savedEntity.setFragile(validRequest.fragile());
        savedEntity.setStatus(validRequest.status());
    }

    @Test
    void createPackage_success() {
        when(pkgMapper.toEntity(validRequest)).thenReturn(pkgEntity);
        when(pkgRepo.save(pkgEntity)).thenReturn(savedEntity);
        when(pkgMapper.toResponseDto(savedEntity))
            .thenReturn(new PackageResponseDTO(
                savedEntity.getPackageId(),
                savedEntity.getDescription(),
                savedEntity.getWeight(),
                savedEntity.getFragile(),
                savedEntity.getStatus()
            ));

        PackageResponseDTO result = service.createPackage(validRequest);

        assertThat(result.packageId()).isEqualTo(savedEntity.getPackageId());
        verify(pkgRepo).save(pkgEntity);
    }

    @Test
    void createPackage_tooHeavy_throws() {
        PackageRequestDTO tooHeavy = new PackageRequestDTO(
            "Hefty item", 60.0, false, PackageStatus.PENDING
        );

        assertThatThrownBy(() -> service.createPackage(tooHeavy))
            .isInstanceOf(APIException.class)
            .hasMessageContaining("Weight must not exceed");

        verifyNoInteractions(pkgRepo, pkgMapper);
    }

    @Test
    void createPackage_invalidStatus_throws() {
        PackageRequestDTO badStatus = new PackageRequestDTO(
            "Fragile item", 5.0, true, PackageStatus.DELIVERED
        );

        assertThatThrownBy(() -> service.createPackage(badStatus))
            .isInstanceOf(APIException.class)
            .hasMessageContaining("Status must be an initial state");

        verifyNoInteractions(pkgRepo, pkgMapper);
    }

    @Test
    void getAllPackages_withDefaultParameters_success() {
        // Given
        List<PackageEntity> entities = createTestEntities();
        Page<PackageEntity> page = new PageImpl<>(entities, 
            org.springframework.data.domain.PageRequest.of(0, 5), 10);
        
        List<PackageResponseDTO> responseDTOs = createTestResponseDTOs();
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(pkgMapper.toResponseDto(any(PackageEntity.class)))
            .thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // When
        PackageResponse result = service.getAllPackages(null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isLastPage()).isFalse();
        
        verify(pkgRepo).findAll(any(Pageable.class));
        verify(pkgMapper, times(2)).toResponseDto(any(PackageEntity.class));
    }

    @Test
    void getAllPackages_withCustomParameters_success() {
        // Given
        List<PackageEntity> entities = createTestEntities();
        Page<PackageEntity> page = new PageImpl<>(entities, 
            org.springframework.data.domain.PageRequest.of(1, 3), 6);
        
        List<PackageResponseDTO> responseDTOs = createTestResponseDTOs();
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(pkgMapper.toResponseDto(any(PackageEntity.class)))
            .thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // When
        PackageResponse result = service.getAllPackages(1, 3, "description", "desc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isLastPage()).isTrue();
        
        verify(pkgRepo).findAll(any(Pageable.class));
    }

    @Test
    void getAllPackages_emptyResult_success() {
        // Given
        Page<PackageEntity> emptyPage = new PageImpl<>(List.of(), 
            org.springframework.data.domain.PageRequest.of(0, 5), 0);
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PackageResponse result = service.getAllPackages(0, 5, "packageId", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isLastPage()).isTrue();
        
        verify(pkgRepo).findAll(any(Pageable.class));
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void getAllPackages_invalidSortDirection_throwsException() {
        // When & Then
        assertThatThrownBy(() -> service.getAllPackages(0, 5, "packageId", "invalid"))
            .isInstanceOf(APIException.class)
            .hasMessageContaining("Invalid sort direction: invalid");
        
        verifyNoInteractions(pkgRepo, pkgMapper);
    }

    @Test
    void getAllPackages_negativePageNumber_usesDefault() {
        // Given
        List<PackageEntity> entities = createTestEntities();
        Page<PackageEntity> page = new PageImpl<>(entities, 
            org.springframework.data.domain.PageRequest.of(0, 5), 2);
        
        List<PackageResponseDTO> responseDTOs = createTestResponseDTOs();
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(pkgMapper.toResponseDto(any(PackageEntity.class)))
            .thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // When
        PackageResponse result = service.getAllPackages(-1, null, null, null);

        // Then
        assertThat(result.getPageNumber()).isZero(); // Should use default
        verify(pkgRepo).findAll(any(Pageable.class));
    }

    @Test
    void getAllPackages_zeroPageSize_usesDefault() {
        // Given
        List<PackageEntity> entities = createTestEntities();
        Page<PackageEntity> page = new PageImpl<>(entities, 
            org.springframework.data.domain.PageRequest.of(0, 5), 2);
        
        List<PackageResponseDTO> responseDTOs = createTestResponseDTOs();
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(pkgMapper.toResponseDto(any(PackageEntity.class)))
            .thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // When
        PackageResponse result = service.getAllPackages(null, 0, null, null);

        // Then
        assertThat(result.getPageSize()).isEqualTo(5); // Should use default
        verify(pkgRepo).findAll(any(Pageable.class));
    }

    @Test
    void getAllPackages_blankSortBy_usesDefault() {
        // Given
        List<PackageEntity> entities = createTestEntities();
        Page<PackageEntity> page = new PageImpl<>(entities, 
            org.springframework.data.domain.PageRequest.of(0, 5), 2);
        
        List<PackageResponseDTO> responseDTOs = createTestResponseDTOs();
        
        when(pkgRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(pkgMapper.toResponseDto(any(PackageEntity.class)))
            .thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // When
        service.getAllPackages(null, null, "  ", null);

        // Then - Verify that the default sort field is used (packageId)
        verify(pkgRepo).findAll(any(Pageable.class));
    }

    @Test
    void getPackageById_existingId_success() {
        // Given
        Long packageId = 1L;
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageId(packageId);
        packageEntity.setDescription("Test Package");
        packageEntity.setWeight(15.0);
        packageEntity.setFragile(true);
        packageEntity.setStatus(PackageStatus.PENDING);

        PackageResponseDTO expectedResponse = new PackageResponseDTO(
                packageId, "Test Package", 15.0, true, PackageStatus.PENDING
        );

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(packageEntity));
        when(pkgMapper.toResponseDto(packageEntity)).thenReturn(expectedResponse);

        // When
        PackageResponseDTO result = service.getPackageById(packageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.packageId()).isEqualTo(packageId);
        assertThat(result.description()).isEqualTo("Test Package");
        assertThat(result.weight()).isEqualTo(15.0);
        assertThat(result.fragile()).isTrue();
        assertThat(result.status()).isEqualTo(PackageStatus.PENDING);

        verify(pkgRepo).findById(packageId);
        verify(pkgMapper).toResponseDto(packageEntity);
    }

    @Test
    void getPackageById_nonExistingId_throwsResourceNotFoundException() {
        // Given
        Long nonExistingId = 999L;
        when(pkgRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getPackageById(nonExistingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Package not found with id: 999");

        verify(pkgRepo).findById(nonExistingId);
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void getPackageById_nullId_throwsResourceNotFoundException() {
        // Given
        Long nullId = null;
        when(pkgRepo.findById(nullId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getPackageById(nullId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Package not found with id: null");

        verify(pkgRepo).findById(nullId);
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void getPackageById_repositoryThrowsException_propagatesException() {
        // Given
        Long packageId = 1L;
        RuntimeException repositoryException = new RuntimeException("Database connection error");
        when(pkgRepo.findById(packageId)).thenThrow(repositoryException);

        // When & Then
        assertThatThrownBy(() -> service.getPackageById(packageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");

        verify(pkgRepo).findById(packageId);
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void getPackageById_mapperReturnsNull_returnsNull() {
        // Given
        Long packageId = 1L;
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageId(packageId);
        packageEntity.setDescription("Test Package");
        packageEntity.setWeight(10.0);
        packageEntity.setFragile(false);
        packageEntity.setStatus(PackageStatus.PENDING);

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(packageEntity));
        when(pkgMapper.toResponseDto(packageEntity)).thenReturn(null);

        // When
        PackageResponseDTO result = service.getPackageById(packageId);

        // Then
        assertThat(result).isNull();

        verify(pkgRepo).findById(packageId);
        verify(pkgMapper).toResponseDto(packageEntity);
    }

    @Test
    void updatePackage_validUpdate_success() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Package Description", 25.0, true, PackageStatus.PROCESSING
        );

        PackageEntity existingEntity = new PackageEntity();
        existingEntity.setPackageId(packageId);
        existingEntity.setDescription("Original Description");
        existingEntity.setWeight(15.0);
        existingEntity.setFragile(false);
        existingEntity.setStatus(PackageStatus.PENDING);

        PackageEntity updatedEntity = new PackageEntity();
        updatedEntity.setPackageId(packageId);
        updatedEntity.setDescription("Updated Package Description");
        updatedEntity.setWeight(25.0);
        updatedEntity.setFragile(true);
        updatedEntity.setStatus(PackageStatus.PROCESSING);

        PackageResponseDTO expectedResponse = new PackageResponseDTO(
                packageId, "Updated Package Description", 25.0, true, PackageStatus.PROCESSING
        );

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(existingEntity));
        when(pkgRepo.save(existingEntity)).thenReturn(updatedEntity);
        when(pkgMapper.toResponseDto(updatedEntity)).thenReturn(expectedResponse);

        // When
        PackageResponseDTO result = service.updatePackage(packageId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.packageId()).isEqualTo(packageId);
        assertThat(result.description()).isEqualTo("Updated Package Description");
        assertThat(result.weight()).isEqualTo(25.0);
        assertThat(result.fragile()).isTrue();
        assertThat(result.status()).isEqualTo(PackageStatus.PROCESSING);

        // Verify entity was updated
        assertThat(existingEntity.getDescription()).isEqualTo("Updated Package Description");
        assertThat(existingEntity.getWeight()).isEqualTo(25.0);
        assertThat(existingEntity.getFragile()).isTrue();
        assertThat(existingEntity.getStatus()).isEqualTo(PackageStatus.PROCESSING);

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo).save(existingEntity);
        verify(pkgMapper).toResponseDto(updatedEntity);
    }

    @Test
    void updatePackage_nonExistingId_throwsResourceNotFoundException() {
        // Given
        Long nonExistingId = 999L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, false, PackageStatus.PENDING
        );

        when(pkgRepo.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(nonExistingId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Package not found with id: 999");

        verify(pkgRepo).findById(nonExistingId);
        verify(pkgRepo, never()).save(any());
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void updatePackage_deliveredPackage_throwsAPIException() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, false, PackageStatus.PENDING
        );

        PackageEntity deliveredEntity = new PackageEntity();
        deliveredEntity.setPackageId(packageId);
        deliveredEntity.setDescription("Delivered Package");
        deliveredEntity.setWeight(15.0);
        deliveredEntity.setFragile(false);
        deliveredEntity.setStatus(PackageStatus.DELIVERED);

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(deliveredEntity));

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(packageId, updateRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Cannot update a package that has already been delivered");

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo, never()).save(any());
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void updatePackage_weightExceedsLimit_throwsAPIException() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 55.0, false, PackageStatus.PENDING // Weight > 50.0
        );

        PackageEntity existingEntity = new PackageEntity();
        existingEntity.setPackageId(packageId);
        existingEntity.setDescription("Original Description");
        existingEntity.setWeight(15.0);
        existingEntity.setFragile(false);
        existingEntity.setStatus(PackageStatus.PENDING);

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(existingEntity));

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(packageId, updateRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Weight must not exceed 50.0 kg");

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo, never()).save(any());
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void updatePackage_invalidStatus_throwsAPIException() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, false, PackageStatus.DELIVERED // Invalid status for update
        );

        PackageEntity existingEntity = new PackageEntity();
        existingEntity.setPackageId(packageId);
        existingEntity.setDescription("Original Description");
        existingEntity.setWeight(15.0);
        existingEntity.setFragile(false);
        existingEntity.setStatus(PackageStatus.PENDING);

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(existingEntity));

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(packageId, updateRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Invalid status transition: from PENDING to DELIVERED");

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo, never()).save(any());
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void updatePackage_inTransitPackage_success() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, true, PackageStatus.OUT_FOR_DELIVERY
        );

        PackageEntity existingEntity = new PackageEntity();
        existingEntity.setPackageId(packageId);
        existingEntity.setDescription("Original Description");
        existingEntity.setWeight(15.0);
        existingEntity.setFragile(false);
        existingEntity.setStatus(PackageStatus.IN_TRANSIT); // Can be updated

        PackageEntity updatedEntity = new PackageEntity();
        updatedEntity.setPackageId(packageId);
        updatedEntity.setDescription("Updated Description");
        updatedEntity.setWeight(20.0);
        updatedEntity.setFragile(true);
        updatedEntity.setStatus(PackageStatus.OUT_FOR_DELIVERY);

        PackageResponseDTO expectedResponse = new PackageResponseDTO(
                packageId, "Updated Description", 20.0, true, PackageStatus.OUT_FOR_DELIVERY
        );

        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(existingEntity));
        when(pkgRepo.save(existingEntity)).thenReturn(updatedEntity);
        when(pkgMapper.toResponseDto(updatedEntity)).thenReturn(expectedResponse);

        // When
        PackageResponseDTO result = service.updatePackage(packageId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("Updated Description");
        assertThat(result.weight()).isEqualTo(20.0);
        assertThat(result.fragile()).isTrue();
        assertThat(result.status()).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo).save(existingEntity);
        verify(pkgMapper).toResponseDto(updatedEntity);
    }

    @Test
    void updatePackage_repositoryThrowsException_propagatesException() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, false, PackageStatus.PENDING
        );

        RuntimeException repositoryException = new RuntimeException("Database connection error");
        when(pkgRepo.findById(packageId)).thenThrow(repositoryException);

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(packageId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo, never()).save(any());
        verifyNoInteractions(pkgMapper);
    }

    @Test
    void updatePackage_saveThrowsException_propagatesException() {
        // Given
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
                "Updated Description", 20.0, false, PackageStatus.PROCESSING
        );

        PackageEntity existingEntity = new PackageEntity();
        existingEntity.setPackageId(packageId);
        existingEntity.setDescription("Original Description");
        existingEntity.setWeight(15.0);
        existingEntity.setFragile(false);
        existingEntity.setStatus(PackageStatus.PENDING);

        RuntimeException saveException = new RuntimeException("Save operation failed");
        when(pkgRepo.findById(packageId)).thenReturn(Optional.of(existingEntity));
        when(pkgRepo.save(existingEntity)).thenThrow(saveException);

        // When & Then
        assertThatThrownBy(() -> service.updatePackage(packageId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Save operation failed");

        verify(pkgRepo).findById(packageId);
        verify(pkgRepo).save(existingEntity);
        verifyNoInteractions(pkgMapper);
    }

    private List<PackageEntity> createTestEntities() {
        PackageEntity entity1 = new PackageEntity();
        entity1.setPackageId(1L);
        entity1.setDescription("Package 1");
        entity1.setWeight(10.0);
        entity1.setFragile(false);
        entity1.setStatus(PackageStatus.PENDING);

        PackageEntity entity2 = new PackageEntity();
        entity2.setPackageId(2L);
        entity2.setDescription("Package 2");
        entity2.setWeight(15.0);
        entity2.setFragile(true);
        entity2.setStatus(PackageStatus.IN_TRANSIT);

        return List.of(entity1, entity2);
    }

    private List<PackageResponseDTO> createTestResponseDTOs() {
        PackageResponseDTO dto1 = new PackageResponseDTO(1L, "Package 1", 10.0, false, PackageStatus.PENDING);
        PackageResponseDTO dto2 = new PackageResponseDTO(2L, "Package 2", 15.0, true, PackageStatus.IN_TRANSIT);
        return List.of(dto1, dto2);
    }
}