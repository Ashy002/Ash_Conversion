package com.ashconversion.service;

import com.ashconversion.config.AppProperties;
import com.ashconversion.exception.FileUploadException;
import com.ashconversion.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    private final String baseUploadDir;
    private final String originalsDir;
    private final FileUtil fileUtil;

    public StorageService(AppProperties appProperties, FileUtil fileUtil) {
        this.baseUploadDir = appProperties.getUpload().getDirectory();
        this.originalsDir = appProperties.getUpload().getOriginalsDirectory();
        this.fileUtil = fileUtil;

        createDirectoryIfNotExists(baseUploadDir);
    }

    /**
     * Stocke un fichier pour un utilisateur
     */
   // StorageService.java
public String storeFile(Long userId, MultipartFile file) throws FileUploadException {
    fileUtil.isValidFileType(file);
    fileUtil.isValidFileSize(file, 104_857_600L);

    String userDir = baseUploadDir + File.separator + userId + File.separator + originalsDir;
    createDirectoryIfNotExists(userDir);

    String uniqueFilename = fileUtil.generateUniqueFilename(file.getOriginalFilename());
    Path filePath = Paths.get(userDir, uniqueFilename);

    try {
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
        throw new FileUploadException("Erreur stockage fichier", e);
    }

    return filePath.toString();
}


    /**
     * Supprime un fichier
     */
    public boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Erreur suppression fichier", e);
            return false;
        }
    }

    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier " + dirPath, e);
        }
    }
}
