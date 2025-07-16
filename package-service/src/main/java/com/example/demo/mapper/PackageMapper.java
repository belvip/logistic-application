package com.example.demo.mapper;

import com.example.demo.model.PackageEntity;
import com.example.demo.payload.PackageRequestDTO;
import com.example.demo.payload.PackageResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PackageMapper {
    PackageEntity toEntity(PackageRequestDTO dto);
    PackageResponseDTO toResponseDto(PackageEntity pkg);
}