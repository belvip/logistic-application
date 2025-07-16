package com.example.demo.repository;


import com.example.demo.model.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<PackageEntity, Long> {
    boolean existsByDescriptionIgnoreCase(String description);
}
