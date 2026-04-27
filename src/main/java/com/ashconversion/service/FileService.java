package com.ashconversion.service;

import com.ashconversion.exception.FileUploadException;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.User;
import com.ashconversion.modele.enums.ConversionType;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final StorageService storageService;
    private final FileJobService fileJobService;
    private final FileUtil fileUtil;

    public FileService(StorageService storageService, FileJobService fileJobService, FileUtil fileUtil) {
        this.storageService = storageService;
        this.fileJobService = fileJobService;
        this.fileUtil = fileUtil;
    }

    /** Upload un fichier et crée un FileJob */
    public FileJob uploadFile(User user, MultipartFile file, String conversionTypeStr) throws FileUploadException {
        // --- TEST DEBUG (DOIT ÊTRE ICI) ---
        System.out.println("!!!! TENTATIVE D'UPLOAD AVEC TYPE : " + conversionTypeStr);

        // 1. Valider le fichier
        fileUtil.isValidFileType(file);
        fileUtil.isValidFileSize(file, 104_857_600L); // 100 MB

        // 2. Déclaration UNIQUE des variables
        String filename = file.getOriginalFilename();
        String extension = fileUtil.getFileExtension(filename);

        // 3. Correction automatique (SÉCURITÉ)
        if (extension != null && (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls"))) {
            if ("WORD_TO_PDF".equals(conversionTypeStr)) {
                logger.warn("Correction forcée : Excel détecté, basculement sur EXCEL_TO_PDF");
                conversionTypeStr = "EXCEL_TO_PDF";
            }
        }

        // 4. Conversion du type (après correction éventuelle)
        ConversionType conversionType;
        try {
            conversionType = ConversionType.valueOf(conversionTypeStr);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException("Type de conversion invalide : " + conversionTypeStr);
        }

        // 5. Vérifier cohérence extension / conversion
        if (!isValidConversionType(extension, conversionType)) {
            throw new FileUploadException(
                "Le type de conversion sélectionné n'est pas compatible avec le type de fichier"
            );
        }

        // 6. Stocker fichier
        String filePath = storageService.storeFile(user.getId(), file);
        String storedFilename = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + 1);

        // 7. Créer et configurer le FileJob
        FileJob fileJob = new FileJob();
        fileJob.setUser(user);
        fileJob.setOriginalFilename(fileUtil.sanitizeFilename(filename));
        fileJob.setStoredFilename(storedFilename);
        fileJob.setFilePath(filePath);
        fileJob.setFileSize(file.getSize());
        fileJob.setMimeType(file.getContentType());
        fileJob.setConversionType(conversionType);
        fileJob.setStatus(ConversionStatus.UPLOADED);

        // 8. Sauvegarder en DB
        FileJob savedFileJob = fileJobService.save(fileJob);

        logger.info("Fichier uploadé avec succès : {} (Type: {}) par {}", filename, conversionType, user.getUsername());
        return savedFileJob;
    }

    private boolean isValidConversionType(String extension, ConversionType conversionType) {
        return switch (conversionType) {
            case PDF_TO_WORD -> "pdf".equalsIgnoreCase(extension);
            case WORD_TO_PDF -> "doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension);
            case IMAGE_TO_PDF -> List.of("png", "jpg", "jpeg", "bmp").contains(extension.toLowerCase());
            case PDF_TO_EXCEL -> "pdf".equalsIgnoreCase(extension);
            case EXCEL_TO_PDF -> "xls".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension);
        };
    }

    public void deleteFile(Long fileId, User user) throws FileUploadException {
        FileJob fileJob = fileJobService.findById(fileId);
        if (fileJob == null) throw new FileUploadException("Fichier non trouvé");
        if (!fileJob.getUser().getId().equals(user.getId()))
            throw new FileUploadException("Vous n'êtes pas autorisé à supprimer ce fichier");

        storageService.deleteFile(fileJob.getFilePath());
        if (fileJob.getOutputPath() != null) storageService.deleteFile(fileJob.getOutputPath());

        fileJobService.delete(fileJob);
        logger.info("Fichier supprimé : {} par {}", fileJob.getOriginalFilename(), user.getUsername());
    }

    public List<FileJob> getUserFiles(User user) {
        return fileJobService.getFileJobs(user, null, null, 0, Integer.MAX_VALUE);
    }

    public List<FileJob> getUserFilesByStatus(User user, ConversionStatus status) {
        return fileJobService.getFileJobs(user, status, null, 0, Integer.MAX_VALUE);
    }
}