package com.ashconversion.service;

import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.modele.enums.ConversionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class ConversionService {

    private static final Logger logger = LoggerFactory.getLogger(ConversionService.class);

    private final PdfToWordService pdfToWordService;
    private final WordToPdfService wordToPdfService;
    private final PdfToExcelService pdfToExcelService; 
    private final FileJobService fileJobService;

    // Constructeur mis à jour avec tous les services
    public ConversionService(PdfToWordService pdfToWordService, 
                             WordToPdfService wordToPdfService, 
                             PdfToExcelService pdfToExcelService,
                             FileJobService fileJobService) {
        this.pdfToWordService = pdfToWordService;
        this.wordToPdfService = wordToPdfService;
        this.pdfToExcelService = pdfToExcelService;
        this.fileJobService = fileJobService;
    }

  public void startConversion(FileJob fileJob) {
        try {
            // 1. Détermination de l'extension
            String extension = switch (fileJob.getConversionType()) {
                case WORD_TO_PDF -> ".pdf";
                case PDF_TO_WORD -> ".docx";
                case PDF_TO_EXCEL -> ".xlsx";
                default -> throw new IllegalArgumentException("Type inconnu");
            };

            // 2. Gestion des chemins
            String inputPath = fileJob.getFilePath();
            String outPath = inputPath.replace("originals", "converted")
                                     .replaceAll("\\.[a-zA-Z0-9]+$", extension);

            fileJob.setOutputPath(outPath);
            fileJob.setOutputFilename(new File(outPath).getName());

            // 3. Création du dossier si inexistant
            File directory = new File(outPath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            // 4. Appel des services (vérifie bien que les méthodes existent !)
            File source = new File(inputPath);
            File target = new File(outPath);

            logger.info("Lancement conversion {} pour Job {}", fileJob.getConversionType(), fileJob.getId());

            switch (fileJob.getConversionType()) {
                case PDF_TO_WORD  -> pdfToWordService.convert(fileJob); 
                case WORD_TO_PDF  -> wordToPdfService.convert(fileJob); 
                case PDF_TO_EXCEL -> pdfToExcelService.convert(source, target);
            }

            // 5. SAUVEGARDE DU SUCCÈS (Très important !)
            fileJob.setStatus(ConversionStatus.COMPLETED);
            fileJob.setProcessedAt(LocalDateTime.now());
            fileJob.setErrorMessage(null); // On nettoie d'anciennes erreurs
            fileJobService.update(fileJob); 
            
            logger.info("Job {} terminé avec succès !", fileJob.getId());

        } catch (Exception e) {
            logger.error("ERREUR CRITIQUE sur le Job ID " + fileJob.getId(), e);
            fileJob.setStatus(ConversionStatus.FAILED);
            fileJob.setErrorMessage("Erreur technique: " + e.getMessage());
            fileJobService.update(fileJob);
        }
    }
}