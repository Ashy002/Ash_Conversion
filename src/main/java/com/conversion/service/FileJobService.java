package com.Ash_Conversion.service;

import com.Ash_Conversion.dao.FileJobDAO;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.model.enums.ConversionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des FileJob.
 * Fournit la logique métier pour les opérations sur les fichiers.
 */
public class FileJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileJobService.class);
    private final FileJobDAO fileJobDAO;
    
    public FileJobService() {
        this.fileJobDAO = new FileJobDAO();
    }
    
    /**
     * Récupère les statistiques pour un utilisateur.
     * 
     * @param user L'utilisateur
     * @return Map avec les statistiques (total, converted, pending, processing, failed)
     */
    public Map<String, Long> getStats(User user) {
        Map<String, Long> stats = new HashMap<>();
        
        try {
            long total = fileJobDAO.countByUserWithFilters(user, null, null);
            long converted = fileJobDAO.countByUserAndStatus(user, ConversionStatus.COMPLETED);
            long uploaded = fileJobDAO.countByUserAndStatus(user, ConversionStatus.UPLOADED);
            long failed = fileJobDAO.countByUserAndStatus(user, ConversionStatus.FAILED);
            
            stats.put("total", total);
            stats.put("converted", converted);
            stats.put("uploaded", uploaded);
            stats.put("failed", failed);
            
            logger.debug("Statistiques récupérées pour l'utilisateur: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques", e);
            // Retourner des stats à zéro en cas d'erreur
            stats.put("total", 0L);
            stats.put("converted", 0L);
            stats.put("failed", 0L);
        }
        
        return stats;
    }
    
    /**
     * Récupère la liste paginée des FileJob avec filtres.
     * 
     * @param user L'utilisateur
     * @param status Le statut (null pour tous)
     * @param searchTerm Le terme de recherche (null pour tous)
     * @param page Le numéro de page (0-based)
     * @param pageSize La taille de la page
     * @return La liste des FileJob
     */
    public List<FileJob> getFileJobs(User user, ConversionStatus status, 
                                    String searchTerm, int page, int pageSize) {
        try {
            return fileJobDAO.findByUserWithFilters(user, status, searchTerm, page, pageSize);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des FileJob", e);
            return List.of();
        }
    }
    
    /**
     * Compte le nombre total de FileJob avec filtres.
     * 
     * @param user L'utilisateur
     * @param status Le statut (null pour tous)
     * @param searchTerm Le terme de recherche (null pour tous)
     * @return Le nombre total
     */
    public long countFileJobs(User user, ConversionStatus status, String searchTerm) {
        try {
            return fileJobDAO.countByUserWithFilters(user, status, searchTerm);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des FileJob", e);
            return 0;
        }
    }
    
    /**
     * Trouve un FileJob par son ID.
     * 
     * @param id L'ID du FileJob
     * @return Le FileJob ou null
     */
    public FileJob findById(Long id) {
        try {
            return fileJobDAO.findById(id);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche du FileJob par ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Trouve un FileJob par son ID et vérifie que l'utilisateur est le propriétaire.
     * 
     * @param fileId L'ID du FileJob
     * @param userId L'ID de l'utilisateur
     * @return Le FileJob si trouvé et propriétaire, null sinon
     */
    public FileJob findByIdAndOwner(Long fileId, Long userId) {
        if (fileId == null || userId == null) {
            return null;
        }
        
        FileJob fileJob = findById(fileId);
        if (fileJob == null) {
            return null;
        }
        
        // Vérifier que l'utilisateur est le propriétaire
        if (!fileJob.getUser().getId().equals(userId)) {
            logger.warn("Tentative d'accès non autorisé: FileJob {} par utilisateur {}", fileId, userId);
            return null;
        }
        
        return fileJob;
    }
    
    /**
     * Vérifie qu'un utilisateur peut accéder à un FileJob.
     * 
     * @param fileJob Le FileJob
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur peut accéder au fichier
     */
    public boolean validateFileAccess(FileJob fileJob, Long userId) {
        if (fileJob == null || userId == null) {
            return false;
        }
        
        return fileJob.getUser().getId().equals(userId);
    }
    
    /**
     * Crée un nouveau FileJob.
     * 
     * @param fileJob Le FileJob à créer
     * @return Le FileJob créé
     */
    public FileJob create(FileJob fileJob) {
        try {
            return fileJobDAO.create(fileJob);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du FileJob", e);
            throw new RuntimeException("Erreur lors de la création du FileJob", e);
        }
    }
    
    /**
     * Met à jour un FileJob.
     * 
     * @param fileJob Le FileJob à mettre à jour
     */
    public void update(FileJob fileJob) {
        try {
            fileJobDAO.update(fileJob);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du FileJob", e);
            throw new RuntimeException("Erreur lors de la mise à jour du FileJob", e);
        }
    }
    
    /**
     * Supprime un FileJob.
     * 
     * @param fileJob Le FileJob à supprimer
     */
    public void delete(FileJob fileJob) {
        try {
            fileJobDAO.delete(fileJob);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du FileJob", e);
            throw new RuntimeException("Erreur lors de la suppression du FileJob", e);
        }
    }
}

