package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.FileUploadException;
import com.Ash_Conversion.util.ConfigUtil;
import com.Ash_Conversion.util.FileUtil;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service pour la gestion du stockage des fichiers.
 * Gère la création des dossiers et le stockage sécurisé des fichiers.
 */
public class StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    
    private final String baseUploadDir;
    private final String originalsDir;
    
    public StorageService() {
        // Récupérer le répertoire de base depuis la config
        String uploadDir = ConfigUtil.getProperty("upload.directory", "storage/uploads");
        String originalsSubDir = ConfigUtil.getProperty("upload.originals.directory", "originals");
        
        this.baseUploadDir = uploadDir;
        this.originalsDir = originalsSubDir;
        
        // Créer le répertoire de base s'il n'existe pas
        createDirectoryIfNotExists(uploadDir);
    }
    
    /**
     * Stocke un fichier uploadé dans le répertoire de l'utilisateur.
     * Structure: storage/uploads/{userId}/originals/{uniqueFilename}
     * 
     * @param userId L'ID de l'utilisateur
     * @param filePart Le Part du fichier uploadé
     * @return Le chemin relatif du fichier stocké
     * @throws FileUploadException si le stockage échoue
     */
    public String storeFile(Long userId, Part filePart) throws FileUploadException {
        try {
            // Valider le fichier
            FileUtil.isValidFileType(filePart);
            FileUtil.isValidFileSize(filePart);
            
            // Créer le répertoire utilisateur
            String userDir = baseUploadDir + File.separator + userId + File.separator + originalsDir;
            createDirectoryIfNotExists(userDir);
            
            // Générer un nom de fichier unique
            String originalFilename = filePart.getSubmittedFileName();
            String uniqueFilename = FileUtil.generateUniqueFilename(originalFilename);
            
            // Chemin complet du fichier
            Path filePath = Paths.get(userDir, uniqueFilename);
            
            // Copier le fichier
            Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Fichier stocké avec succès: {} pour l'utilisateur {}", uniqueFilename, userId);
            
            // Retourner le chemin relatif (pour stockage en DB)
            return userDir + File.separator + uniqueFilename;
            
        } catch (IOException e) {
            logger.error("Erreur lors du stockage du fichier", e);
            throw new FileUploadException("Erreur lors du stockage du fichier: " + e.getMessage());
        }
    }
    
    /**
     * Supprime un fichier du stockage.
     * 
     * @param filePath Le chemin du fichier à supprimer
     * @return true si la suppression a réussi
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.debug("Fichier supprimé: {}", filePath);
                return true;
            } else {
                logger.warn("Fichier non trouvé pour suppression: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Récupère le chemin absolu d'un fichier.
     * 
     * @param relativePath Le chemin relatif
     * @return Le chemin absolu
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(relativePath);
    }
    
    /**
     * Vérifie si un fichier existe.
     * 
     * @param filePath Le chemin du fichier
     * @return true si le fichier existe
     */
    public boolean fileExists(String filePath) {
        try {
            return Files.exists(Paths.get(filePath));
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'existence du fichier: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Crée un répertoire s'il n'existe pas.
     * 
     * @param dirPath Le chemin du répertoire
     */
    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.debug("Répertoire créé: {}", dirPath);
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la création du répertoire: {}", dirPath, e);
            throw new RuntimeException("Impossible de créer le répertoire: " + dirPath, e);
        }
    }
}

