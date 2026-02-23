package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.exception.ConversionException;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.model.enums.ConversionType;
import com.Ash_Conversion.service.ConversionService;
import com.Ash_Conversion.service.FileJobService;
import com.Ash_Conversion.service.UserService;
import com.Ash_Conversion.util.FlashMessageUtil;
import com.Ash_Conversion.util.JsonResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet pour gérer les conversions de fichiers.
 * Endpoint: /convert?id=...&to=docx|pdf|xlsx
 */
@WebServlet(name = "ConversionServlet", urlPatterns = {RouteConstants.API_CONVERT})
public class ConversionServlet extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversionServlet.class);
    private final ConversionService conversionService = new ConversionService();
    private final FileJobService fileJobService = new FileJobService();
    private final UserService userService = new UserService();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Vérifier le token CSRF
        if (!validateCsrfToken(request, response)) {
            return; // Erreur déjà envoyée
        }
        
        Long userId = requireAuthenticatedUser(request, response);
        if (userId == null) {
            return; // Erreur déjà envoyée
        }
        
        try {
            // Récupérer les paramètres
            String toParam = request.getParameter("to");
            if (toParam == null) {
                JsonResponseUtil.sendError(response, "Paramètre 'to' requis", 
                                         HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long fileId = parseFileId(request, response);
            if (fileId == null) {
                return; // Erreur déjà envoyée
            }
            
            // Récupérer le FileJob et vérifier la propriété
            FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
            if (fileJob == null) {
                JsonResponseUtil.sendError(response, "Fichier introuvable ou accès non autorisé", 
                                         HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Vérifier que le fichier peut être converti
            if (fileJob.getStatus() != ConversionStatus.UPLOADED && 
                fileJob.getStatus() != ConversionStatus.FAILED) {
                JsonResponseUtil.sendError(response, 
                    "Le fichier ne peut pas être converti dans son état actuel (statut: " + 
                    fileJob.getStatus() + ")", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Mapper le paramètre "to" vers ConversionType
            ConversionType targetType = mapToConversionType(fileJob.getMimeType(), toParam);
            if (targetType == null) {
                JsonResponseUtil.sendError(response, 
                    "Type de conversion invalide ou incompatible. Types acceptés: docx, pdf, xlsx", 
                    HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Mettre à jour le type de conversion si nécessaire
            if (fileJob.getConversionType() != targetType) {
                fileJob.setConversionType(targetType);
                fileJobService.update(fileJob);
            }
            
            // Démarrer la conversion asynchrone
            conversionService.startConversion(fileJob);
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileJobId", fileJob.getId());
            data.put("status", "PROCESSING");
            data.put("message", "Conversion démarrée");
            
            JsonResponseUtil.sendSuccess(response, data);
            logger.info("Conversion démarrée pour FileJob: {} par utilisateur: {}", 
                       fileId, userId);
            
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de la conversion", e);
            JsonResponseUtil.sendError(response, "Erreur lors du démarrage de la conversion", 
                                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Vérifier l'authentification
        Long userId = requireAuthenticatedUser(request, response);
        if (userId == null) {
            return; // Erreur déjà envoyée
        }
        
        Long fileId = parseFileId(request, response);
        if (fileId == null) {
            return; // Erreur déjà envoyée
        }
        
        try {
            // Récupérer le FileJob et vérifier la propriété (owner check)
            FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
            if (fileJob == null) {
                JsonResponseUtil.sendError(response, "Fichier introuvable ou accès non autorisé", 
                                         HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileJobId", fileJob.getId());
            data.put("status", fileJob.getStatus().toString());
            data.put("errorMessage", fileJob.getErrorMessage());
            
            JsonResponseUtil.sendSuccess(response, data);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du statut", e);
            JsonResponseUtil.sendError(response, "Erreur lors de la récupération du statut", 
                                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Mappe le type MIME du fichier source et le paramètre "to" vers un ConversionType.
     */
    private ConversionType mapToConversionType(String mimeType, String to) {
        if (mimeType == null || to == null) {
            return null;
        }
        
        String toLower = to.toLowerCase();
        
        // PDF → Word
        if (mimeType.contains("pdf") && "docx".equals(toLower)) {
            return ConversionType.PDF_TO_WORD;
        }
        
        // Word → PDF
        if ((mimeType.contains("word") || mimeType.contains("document")) && "pdf".equals(toLower)) {
            return ConversionType.WORD_TO_PDF;
        }
        
        // PDF → Excel
        if (mimeType.contains("pdf") && "xlsx".equals(toLower)) {
            return ConversionType.PDF_TO_EXCEL;
        }
        
        // Excel → PDF
        if ((mimeType.contains("excel") || mimeType.contains("spreadsheet")) && "pdf".equals(toLower)) {
            return ConversionType.EXCEL_TO_PDF;
        }
        
        return null;
    }
}

