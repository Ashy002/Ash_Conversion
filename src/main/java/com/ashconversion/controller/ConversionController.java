package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.modele.enums.ConversionType;
import com.ashconversion.service.ConversionService;
import com.ashconversion.service.FileJobService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST pour gérer les conversions de fichiers.
 * Migration du ConversionServlet Java EE vers Spring Boot.
 *
 * Endpoint:
 * POST /api/convert?id=...&to=docx|pdf|xlsx
 * GET  /api/convert?id=...
 */
@RestController
@RequestMapping(RouteConstants.API_CONVERT)
public class ConversionController {

    private static final Logger logger = LoggerFactory.getLogger(ConversionController.class);
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";

    private final ConversionService conversionService;
    private final FileJobService fileJobService;

    public ConversionController(
            ConversionService conversionService,
            FileJobService fileJobService
    ) {
        this.conversionService = conversionService;
        this.fileJobService = fileJobService;
    }

    /**
     * Démarre une conversion de fichier.
     */
   /**
     * Démarre une conversion de fichier.
     */
    @PostMapping
    public ResponseEntity<?> startConversion(
            @RequestParam("id") Long fileId,
            @RequestParam("to") String to,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute(SESSION_USER_ID_ATTRIBUTE);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Non authentifié"));
        }

        FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
        if (fileJob == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Fichier introuvable"));
        }

        // Vérification du statut
        if (fileJob.getStatus() != ConversionStatus.UPLOADED && fileJob.getStatus() != ConversionStatus.FAILED) {
            return ResponseEntity.badRequest().body(error("Le fichier ne peut pas être converti dans son état actuel"));
        }

        // Détermination du type de conversion
        ConversionType targetType = mapToConversionType(fileJob.getMimeType(), to);
        if (targetType == null) {
            return ResponseEntity.badRequest().body(error("Type de conversion incompatible"));
        }

        // --- CORRECTION MAJEURE : Préparation des chemins ---
        fileJob.setConversionType(targetType);
        
        // Si le chemin de sortie est nul, on le génère
        if (fileJob.getOutputPath() == null || fileJob.getOutputPath().isBlank()) {
            String inputPath = fileJob.getFilePath();
            String extension = (targetType == ConversionType.WORD_TO_PDF) ? ".pdf" : ".docx";
            
            // On remplace le dossier 'originals' par 'converted' et on change l'extension
            String outPath = inputPath.replace("originals", "converted")
                                     .replaceAll("\\.[a-zA-Z0-9]+$", extension);
            
            fileJob.setOutputPath(outPath);
            fileJob.setOutputFilename(new java.io.File(outPath).getName());
        }

        // Mise à jour du statut avant lancement
        fileJob.setStatus(ConversionStatus.PROCESSING);
        fileJobService.update(fileJob);

        // Appel du service (le moteur de conversion)
        conversionService.startConversion(fileJob);

        // Préparation de la réponse JSON
        Map<String, Object> data = new HashMap<>();
        data.put("fileJobId", fileJob.getId());
        data.put("status", "PROCESSING");
        data.put("message", "Conversion démarrée");

        logger.info("Conversion démarrée pour FileJob {} par utilisateur {}", fileId, userId);
        return ResponseEntity.ok(success(data));
    }

    /**
     * Mapping MIME + paramètre "to" → ConversionType.
     */
    private ConversionType mapToConversionType(String mimeType, String to) {
        if (mimeType == null || to == null) return null;

        to = to.toLowerCase();

        if (mimeType.contains("pdf") && to.equals("docx")) {
            return ConversionType.PDF_TO_WORD;
        }
        if ((mimeType.contains("word") || mimeType.contains("document")) && to.equals("pdf")) {
            return ConversionType.WORD_TO_PDF;
        }
        if (mimeType.contains("pdf") && to.equals("xlsx")) {
            return ConversionType.PDF_TO_EXCEL;
        }
        if ((mimeType.contains("excel") || mimeType.contains("spreadsheet")) && to.equals("pdf")) {
            return ConversionType.EXCEL_TO_PDF;
        }
        return null;
    }

    /* =======================
       Helpers JSON standard
       ======================= */

    private Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("error", message);
        return map;
    }
}
