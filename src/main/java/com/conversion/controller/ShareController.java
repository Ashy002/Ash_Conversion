package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.exception.ShareException;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.ShareToken;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.service.FileJobService;
import com.Ash_Conversion.service.ShareService;
import com.Ash_Conversion.util.FileStreamUtil;
import com.Ash_Conversion.util.JsonResponseUtil;
import com.Ash_Conversion.util.MimeTypeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet pour gérer le partage de fichiers via tokens.
 * Endpoints:
 * - POST /api/share : Génère un token de partage
 * - GET /share/{token} : Accède au fichier partagé (public)
 */
@WebServlet(name = "ShareController", urlPatterns = {RouteConstants.API_SHARE, RouteConstants.SHARE_PUBLIC + "/*"})
public class ShareController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    private final ShareService shareService = new ShareService();
    private final FileJobService fileJobService = new FileJobService();
    
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
            
            // Générer le token de partage
            ShareToken shareToken = shareService.generateShareToken(fileJob);
            
            // Construire l'URL de partage
            String shareUrl = request.getScheme() + "://" + request.getServerName() + 
                            (request.getServerPort() != 80 && request.getServerPort() != 443 ? 
                             ":" + request.getServerPort() : "") +
                            request.getContextPath() + RouteConstants.SHARE_PUBLIC + "/" + shareToken.getToken();
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", shareToken.getToken());
            data.put("url", shareUrl);
            data.put("expiresAt", shareToken.getExpiresAt().toString());
            data.put("maxAccess", shareToken.getMaxAccess());
            
            JsonResponseUtil.sendSuccess(response, data);
            logger.info("Token de partage généré pour FileJob: {} par utilisateur: {}", 
                       fileId, userId);
            
        } catch (ShareException e) {
            logger.warn("Erreur lors de la génération du token: {}", e.getMessage());
            JsonResponseUtil.sendError(response, e.getMessage(), 
                                     HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du token de partage", e);
            JsonResponseUtil.sendError(response, "Erreur lors de la génération du token", 
                                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token manquant");
            return;
        }
        
        String token = pathInfo.substring(1); // Enlever le "/" initial
        
        try {
            // Récupérer le FileJob via le token
            FileJob fileJob = shareService.getFileByToken(token);
            
            // Vérifier que le fichier est converti
            if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Le fichier n'est pas encore converti");
                return;
            }
            
            // Vérifier que le fichier existe
            File outputFile = new File(fileJob.getOutputPath());
            if (!outputFile.exists()) {
                logger.error("Fichier converti introuvable: {}", fileJob.getOutputPath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier introuvable");
                return;
            }
            
            // Stream le fichier
            String mimeType = MimeTypeUtil.getMimeType(fileJob.getOutputFilename());
            FileStreamUtil.streamFile(outputFile, response, mimeType, 
                                     fileJob.getOutputFilename(), false);
            
            logger.info("Fichier partagé téléchargé via token: {} (FileJob: {})", 
                       token, fileJob.getId());
            
        } catch (ShareException e) {
            logger.warn("Erreur lors de l'accès au fichier partagé: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès au fichier partagé", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                              "Erreur lors de l'accès au fichier partagé");
        }
    }
}
