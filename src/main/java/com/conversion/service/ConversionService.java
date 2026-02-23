package com.Ash_Conversion.service;

import com.Ash_Conversion.util.ConfigUtil;
import com.Ash_Conversion.exception.ConversionException;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.model.enums.ConversionType;
import com.Ash_Conversion.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service pour la gestion des conversions de fichiers.
 * Utilise ExecutorService pour les conversions asynchrones.
 */
public class ConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversionService.class);
    
    private final ExecutorService executorService;
    private final FileJobService fileJobService;
    private final StorageService storageService;
    private final PdfToWordService pdfToWordService;
    private final WordToPdfService wordToPdfService;
    private final PdfToExcelService pdfToExcelService;
    private final ExcelToPdfService excelToPdfService;
    
    public ConversionService() {
        int poolSize = ConfigUtil.getIntProperty("conversion.thread.pool.size", 5);
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.fileJobService = new FileJobService();
        this.storageService = new StorageService();
        this.pdfToWordService = new PdfToWordService();
        this.wordToPdfService = new WordToPdfService();
        this.pdfToExcelService = new PdfToExcelService();
        this.excelToPdfService = new ExcelToPdfService();
        
        logger.info("ConversionService initialisé avec un pool de {} threads", poolSize);
    }
    
    /**
     * Démarre une conversion de manière asynchrone.
     * 
     * @param fileJob Le FileJob à convertir
     */
    public void startConversion(FileJob fileJob) {
        if (fileJob == null) {
            logger.error("Tentative de conversion d'un FileJob null");
            return;
        }
        
        // Vérifier que le fichier peut être converti
        if (fileJob.getStatus() != ConversionStatus.UPLOADED && 
            fileJob.getStatus() != ConversionStatus.COMPLETED) {
            logger.warn("FileJob {} ne peut pas être converti (statut: {})", 
                       fileJob.getId(), fileJob.getStatus());
            return;
        }
        
        /*// Mettre à jour le statut à PROCESSING
        fileJob.setStatus(ConversionStatus.PROCESSING);
        fileJobService.update(fileJob); */
        
        logger.info("Démarrage de la conversion asynchrone pour FileJob: {}", fileJob.getId());
        
        // Soumettre la conversion au thread pool
        executorService.submit(() -> {
            try {
                performConversion(fileJob);
            } catch (Exception e) {
                logger.error("Erreur lors de la conversion du FileJob: {}", fileJob.getId(), e);
                handleConversionError(fileJob, e);
            }
        });
    }
    
    /**
     * Effectue la conversion du fichier.
     */
    private void performConversion(FileJob fileJob) throws ConversionException {
        logger.debug("Conversion en cours pour FileJob: {}", fileJob.getId());
        
        // Vérifier que le fichier source existe
        File sourceFile = new File(fileJob.getFilePath());
        if (!sourceFile.exists()) {
            throw new ConversionException("Le fichier source n'existe pas: " + fileJob.getFilePath());
        }
        
        // Créer le répertoire de destination
        String convertedDir = ConfigUtil.getProperty("upload.directory", "storage/uploads") + 
                              File.separator + fileJob.getUser().getId() + 
                              File.separator + ConfigUtil.getProperty("upload.converted.directory", "converted");
        File convertedDirFile = new File(convertedDir);
        if (!convertedDirFile.exists()) {
            convertedDirFile.mkdirs();
        }
        
        // Générer le nom du fichier de sortie
        String outputExtension = getOutputExtension(fileJob.getConversionType());
        String outputFilename = FileUtil.generateUniqueFilename(
            fileJob.getOriginalFilename().replaceFirst("\\.[^.]+$", "") + "." + outputExtension);
        File outputFile = new File(convertedDir, outputFilename);
        
        // Effectuer la conversion selon le type
        switch (fileJob.getConversionType()) {
            case PDF_TO_WORD:
                pdfToWordService.convert(sourceFile, outputFile);
                break;
            case WORD_TO_PDF:
                wordToPdfService.convert(sourceFile, outputFile);
                break;
            case PDF_TO_EXCEL:
                pdfToExcelService.convert(sourceFile, outputFile);
                break;
            case EXCEL_TO_PDF:
                excelToPdfService.convert(sourceFile, outputFile);
                break;
            default:
                throw new ConversionException("Type de conversion non supporté: " + fileJob.getConversionType());
        }
        
        // Mettre à jour le FileJob avec le résultat
        fileJob.setStatus(ConversionStatus.COMPLETED);
        fileJob.setOutputFilename(outputFilename);
        fileJob.setOutputPath(outputFile.getAbsolutePath());
        fileJob.setProcessedAt(LocalDateTime.now());
        fileJob.setErrorMessage(null);
        
        fileJobService.update(fileJob);
        
        logger.info("Conversion réussie pour FileJob: {}", fileJob.getId());
    }
    
    /**
     * Gère les erreurs de conversion.
     */
    private void handleConversionError(FileJob fileJob, Exception e) {
        fileJob.setStatus(ConversionStatus.FAILED);
        fileJob.setErrorMessage(e.getMessage());
        fileJob.setProcessedAt(LocalDateTime.now());
        
        fileJobService.update(fileJob);
        
        logger.error("Conversion échouée pour FileJob: {} - Erreur: {}", 
                    fileJob.getId(), e.getMessage());
    }
    
    /**
     * Récupère l'extension du fichier de sortie selon le type de conversion.
     */
    private String getOutputExtension(ConversionType conversionType) {
        return switch (conversionType) {
            case PDF_TO_WORD -> "docx";
            case WORD_TO_PDF -> "pdf";
            case PDF_TO_EXCEL -> "xlsx";
            case EXCEL_TO_PDF -> "pdf";
        };
    }
    
    /**
     * Récupère le statut d'une conversion.
     * 
     * @param fileJobId L'ID du FileJob
     * @return Le statut de conversion
     */
    public ConversionStatus getConversionStatus(Long fileJobId) {
        FileJob fileJob = fileJobService.findById(fileJobId);
        if (fileJob == null) {
            return null;
        }
        return fileJob.getStatus();
    }
    
    /**
     * Arrête le service et libère les ressources.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ConversionService arrêté");
    }
}
