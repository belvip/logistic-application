package com.example.demo.service.impl;

import com.example.demo.config.AppConstant;
import com.example.demo.exceptions.APIException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.PackageMapper;
import com.example.demo.model.PackageEntity;
import com.example.demo.model.PackageStatus;
import com.example.demo.payload.PackageRequestDTO;
import com.example.demo.payload.PackageResponse;
import com.example.demo.payload.PackageResponseDTO;
import com.example.demo.repository.PackageRepository;
import com.example.demo.service.PackageService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final PackageRepository pkgRepo;
    private final PackageMapper pkgMapper;

    @Override
    public PackageResponseDTO createPackage(PackageRequestDTO request) {
        validatePackageRequest(request);
        PackageEntity pkgEntity = pkgMapper.toEntity(request);
        PackageEntity savedEntity = pkgRepo.save(pkgEntity);
        return pkgMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponse getAllPackages(Integer pageNumber, Integer pageSize,
                                          String sortBy, String sortOrder) {
        PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sortBy, sortOrder);
        Pageable pageable = createPageable(pageRequest);
        Page<PackageEntity> pageResult = pkgRepo.findAll(pageable);
        
        List<PackageResponseDTO> content = pageResult.getContent().stream()
                .map(pkgMapper::toResponseDto)
                .toList();

        return buildPackageResponse(pageResult, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponseDTO getPackageById(Long id) {
        PackageEntity pkg = pkgRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Package not found with id: %d", id)
                ));
        return pkgMapper.toResponseDto(pkg);
    }

    @Override
    @Transactional
    public PackageResponseDTO updatePackage(Long id, PackageRequestDTO pkgDTO) {
        PackageEntity existing = pkgRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Package not found with id: %d", id)
                ));

        if (existing.getStatus() == PackageStatus.DELIVERED) {
            throw new APIException("Cannot update a package that has already been delivered");
        }

        validateWeight(pkgDTO.weight());
        validateStatusTransition(existing.getStatus(), pkgDTO.status());

        existing.setDescription(pkgDTO.description());
        existing.setWeight(pkgDTO.weight());
        existing.setFragile(pkgDTO.fragile());
        existing.setStatus(pkgDTO.status());

        PackageEntity updated = pkgRepo.save(existing);
        return pkgMapper.toResponseDto(updated);
    }

    @Override
    public PackageResponseDTO deletePackage(Long id) {
        return null;
    }

    private static final Map<PackageStatus, Set<PackageStatus>> ALLOWED_TRANSITIONS = Map.of(
            PackageStatus.PENDING, EnumSet.of(PackageStatus.PROCESSING),
            PackageStatus.PROCESSING, EnumSet.of(PackageStatus.IN_TRANSIT),
            PackageStatus.IN_TRANSIT, EnumSet.of(PackageStatus.OUT_FOR_DELIVERY),
            PackageStatus.OUT_FOR_DELIVERY, EnumSet.of(PackageStatus.DELIVERED)
    );

    private void validateStatusTransition(PackageStatus current, PackageStatus next) {
        Set<PackageStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        if (!allowed.contains(next)) {
            throw new APIException("Invalid status transition: from " + current + " to " + next);
        }
    }

    private Pageable createPageable(PageRequest request) {
        int page = getValidPage(request.pageNumber());
        int size = getValidSize(request.pageSize());
        String field = getValidSortField(request.sortBy());
        Sort.Direction direction = getSortDirection(request.sortOrder());
        
        return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(direction, field));
    }

    private int getValidPage(Integer pageNumber) {
        return pageNumber != null && pageNumber >= 0 ? pageNumber : Integer.parseInt(AppConstant.PAGE_NUMBER);
    }

    private int getValidSize(Integer pageSize) {
        return pageSize != null && pageSize > 0 ? pageSize : Integer.parseInt(AppConstant.PAGE_SIZE);
    }

    private String getValidSortField(String sortBy) {
        return sortBy != null && !sortBy.isBlank() ? sortBy : AppConstant.SORT_PACKAGE_BY;
    }

    private Sort.Direction getSortDirection(String sortOrder) {
        String dir = getValidSortOrder(sortOrder);
        try {
            return Sort.Direction.fromString(dir);
        } catch (IllegalArgumentException e) {
            throw new APIException("Invalid sort direction: " + dir);
        }
    }

    private String getValidSortOrder(String sortOrder) {
        return sortOrder != null && !sortOrder.isBlank() ? sortOrder : AppConstant.SORT_DIR;
    }

    private PackageResponse buildPackageResponse(Page<PackageEntity> pageResult, List<PackageResponseDTO> content) {
        return new PackageResponse(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }


    private void validatePackageRequest(PackageRequestDTO request) {
        validateWeight(request.weight());
        validateStatus(request.status());
    }

    private void validateWeight(double weight) {
        if (weight > 50.0) {
            throw new APIException("Weight must not exceed 50.0 kg");
        }
    }

    private void validateStatus(PackageStatus status) {
        if (status != PackageStatus.PENDING) {
            throw new APIException("Status must be an initial state (e.g. PENDING)");
        }
    }

    public record PageRequest(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {}





}