package com.ashconversion.repository;

import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.User;
import com.ashconversion.modele.enums.ConversionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileJobRepository extends JpaRepository<FileJob, Long> {

    // Recherche par utilisateur et pagination
    Page<FileJob> findByUser(User user, Pageable pageable);

    // Recherche par utilisateur + statut + pagination
    Page<FileJob> findByUserAndStatus(User user, ConversionStatus status, Pageable pageable);

    // Recherche par utilisateur + nom fichier partiel (ignore case)
    Page<FileJob> findByUserAndOriginalFilenameContainingIgnoreCase(User user, String search, Pageable pageable);

    // Recherche par utilisateur + statut + nom fichier partiel
    Page<FileJob> findByUserAndStatusAndOriginalFilenameContainingIgnoreCase(User user, ConversionStatus status, String search, Pageable pageable);

    Optional<FileJob> findByIdAndUserId(Long id, Long userId);

    // Comptage total
    long countByUser(User user);

    long countByUserAndStatus(User user, ConversionStatus status);

    long countByUserAndOriginalFilenameContainingIgnoreCase(User user, String search);

    long countByUserAndStatusAndOriginalFilenameContainingIgnoreCase(User user, ConversionStatus status, String search);
}
