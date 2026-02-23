package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.FileUploadException;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.model.enums.ConversionType;
import com.Ash_Conversion.util.FileUtil;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour la gestion des fichiers (upload, récupération, suppression).
 */
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    private final StorageService storageService;
    private final FileJobService fileJobService;
    
    public FileService() {
        this.storageService = new StorageService();
        this.fileJobService = new FileJobService();
    }
    
    /**
     * Upload un fichier et crée un FileJob.
     * 
     * @param user L'utilisateur propriétaire
     * @param filePart Le Part du fichier uploadé
     * @param conversionTypeStr Le type de conversion (string)
     * @return Le FileJob créé
     * @throws FileUploadException si l'upload échoue
     */
    public FileJob uploadFile(User user, Part filePart, String conversionTypeStr) 
            throws FileUploadException {
        
        // Valider le fichier
        FileUtil.isValidFileType(filePart);
        FileUtil.isValidFileSize(filePart);
        
        // Valider et convertir le type de conversion
        ConversionType conversionType;
        try {
            conversionType = ConversionType.valueOf(conversionTypeStr);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException("Type de conversion invalide: " + conversionTypeStr);
        }
        
        // Vérifier la cohérence type fichier / type conversion
        String filename = filePart.getSubmittedFileName();
        String extension = FileUtil.getFileExtension(filename);
        
        if (!isValidConversionType(extension, conversionType)) {
            throw new FileUploadException(
                "Le type de conversion sélectionné n'est pas compatible avec le type de fichier");
        }
        
        // Stocker le fichier
        String filePath = storageService.storeFile(user.getId(), filePart);
        String storedFilename = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + 1);
        
        // Créer le FileJob
        FileJob fileJob = new FileJob();
        fileJob.setUser(user);
        fileJob.setOriginalFilename(FileUtil.sanitizeFilename(filename));
        fileJob.setStoredFilename(storedFilename);
        fileJob.setFilePath(filePath);
        fileJob.setFileSize(filePart.getSize());
        fileJob.setMimeType(filePart.getContentType());
        fileJob.setConversionType(conversionType);
        fileJob.setStatus(ConversionStatus.UPLOADED);
        fileJob.setCreatedAt(LocalDateTime.now());
        
        // Sauvegarder en DB
        FileJob savedFileJob = fileJobService.create(fileJob);
        
        logger.info("Fichier uploadé avec succès: {} par l'utilisateur {}", filename, user.getUsername());
        
        return savedFileJob;
    }
    
    /**
     * Vérifie si le type de conversion est compatible avec l'extension du fichier.
     */
    private boolean isValidConversionType(String extension, ConversionType conversionType) {
        return switch (conversionType) {
            case PDF_TO_WORD -> "pdf".equals(extension);
            case WORD_TO_PDF -> "docx".equals(extension) || "doc".equals(extension);
            case PDF_TO_EXCEL -> "pdf".equals(extension);
            case EXCEL_TO_PDF -> "xlsx".equals(extension) || "xls".equals(extension);
        };
    }
    
    /**
     * Récupère tous les fichiers d'un utilisateur.
     */
    public List<FileJob> getUserFiles(User user) {
        return fileJobService.getFileJobs(user, null, null, 0, Integer.MAX_VALUE);
    }
    
    /**
     * Récupère les fichiers d'un utilisateur par statut.
     */
    public List<FileJob> getUserFilesByStatus(User user, ConversionStatus status) {
        return fileJobService.getFileJobs(user, status, null, 0, Integer.MAX_VALUE);
    }
    
    /**
     * Supprime un fichier (physique et en DB).
     * Vérifie que l'utilisateur est propriétaire.
     */
    public void deleteFile(Long fileId, User user) throws FileUploadException {
        FileJob fileJob = fileJobService.findById(fileId);
        
        if (fileJob == null) {
            throw new FileUploadException("Fichier non trouvé");
        }
        
        // Vérifier que l'utilisateur est propriétaire
        if (!fileJob.getUser().getId().equals(user.getId())) {
            throw new FileUploadException("Vous n'êtes pas autorisé à supprimer ce fichier");
        }
        
        // Supprimer le fichier physique
        storageService.deleteFile(fileJob.getFilePath());
        if (fileJob.getOutputPath() != null) {
            storageService.deleteFile(fileJob.getOutputPath());
        }
        
        // Supprimer de la DB
        fileJobService.delete(fileJob);
        
        logger.info("Fichier supprimé: {} par l'utilisateur {}", fileJob.getOriginalFilename(), user.getUsername());
    }
}
