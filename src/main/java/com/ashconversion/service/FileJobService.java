package com.ashconversion.service;

import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.User;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.repository.FileJobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FileJobService {

    private final FileJobRepository fileJobRepository;

    public FileJobService(FileJobRepository fileJobRepository) {
        this.fileJobRepository = fileJobRepository;
    }

    /** Récupère un FileJob par son ID */
    public FileJob findById(Long id) {
        return fileJobRepository.findById(id).orElse(null);
    }

    /** Récupère un FileJob par son ID et propriétaire */
    public FileJob findByIdAndOwner(Long fileId, Long userId) {
        return fileJobRepository.findByIdAndUserId(fileId, userId).orElse(null);
    }

    /** Enregistre un nouveau FileJob */
    public FileJob save(FileJob fileJob) {
        return fileJobRepository.save(fileJob);
    }

    /** Met à jour un FileJob existant */
    public FileJob update(FileJob fileJob) {
        return fileJobRepository.save(fileJob);
    }

    /** Supprime un FileJob */
    public void delete(FileJob fileJob) {
        fileJobRepository.delete(fileJob);
    }

    /**
     * Récupère les fichiers d’un utilisateur avec pagination et filtre par status + recherche
     *
     * @param user   utilisateur propriétaire
     * @param status statut du fichier (nullable)
     * @param search texte de recherche dans le nom original du fichier (nullable)
     * @param page   numéro de page (0-based)
     * @param size   taille de page
     */
    public List<FileJob> getFileJobs(User user, ConversionStatus status, String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FileJob> result;

        if ((search == null || search.isEmpty()) && status == null) {
            result = fileJobRepository.findByUser(user, pageRequest);
        } else if ((search == null || search.isEmpty())) {
            result = fileJobRepository.findByUserAndStatus(user, status, pageRequest);
        } else if (status == null) {
            result = fileJobRepository.findByUserAndOriginalFilenameContainingIgnoreCase(user, search, pageRequest);
        } else {
            result = fileJobRepository.findByUserAndStatusAndOriginalFilenameContainingIgnoreCase(user, status, search, pageRequest);
        }

        return result.getContent();
    }

    /**
     * Compte le nombre de fichiers pour un utilisateur avec filtre status + recherche
     */
    public long countFileJobs(User user, ConversionStatus status, String search) {
        if ((search == null || search.isEmpty()) && status == null) {
            return fileJobRepository.countByUser(user);
        } else if ((search == null || search.isEmpty())) {
            return fileJobRepository.countByUserAndStatus(user, status);
        } else if (status == null) {
            return fileJobRepository.countByUserAndOriginalFilenameContainingIgnoreCase(user, search);
        } else {
            return fileJobRepository.countByUserAndStatusAndOriginalFilenameContainingIgnoreCase(user, status, search);
        }
    }

    /**
     * Statistiques simples : nombre de fichiers par statut pour un utilisateur
     */
    public Map<String, Long> getStats(User user) {
    long total = fileJobRepository.countByUser(user);
    long completed = fileJobRepository.countByUserAndStatus(user, ConversionStatus.COMPLETED);
    long failed = fileJobRepository.countByUserAndStatus(user, ConversionStatus.FAILED);

    
    return Map.of(
            "total", total,
            "converted", completed,
            "failed", failed
    );
}
}
