package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.exception.FileUploadException;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.service.FileService;
import com.Ash_Conversion.service.UserService;
import com.Ash_Conversion.util.JsonResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet pour supprimer des fichiers via API.
 * Endpoint: DELETE /api/files/{id}
 */
@WebServlet(name = "FileDeleteServlet", urlPatterns = {RouteConstants.API_FILES + "/*"})
public class FileDeleteServlet extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileDeleteServlet.class);
    private final FileService fileService = new FileService();
    private final UserService userService = new UserService();
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier le token CSRF
        if (!validateCsrfToken(request, response)) {
            return; // Erreur déjà envoyée
        }
        
        // Vérifier l'authentification
        Long userId = requireAuthenticatedUser(request, response);
        if (userId == null) {
            return; // Erreur déjà envoyée
        }
        
        try {
            // Extraire l'ID depuis le path (ex: /api/files/123 -> 123)
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponseUtil.sendError(response, "ID de fichier requis", 
                                         HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String fileIdStr = pathInfo.substring(1); // Enlever le "/" initial
            Long fileId;
            try {
                fileId = Long.parseLong(fileIdStr);
            } catch (NumberFormatException e) {
                JsonResponseUtil.sendError(response, "ID de fichier invalide", 
                                         HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Récupérer l'utilisateur
            User user = userService.findById(userId);
            if (user == null) {
                JsonResponseUtil.sendError(response, "Utilisateur introuvable", 
                                         HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Supprimer le fichier
            fileService.deleteFile(fileId, user);
            
            JsonResponseUtil.sendSuccess(response, Map.of("message", "Fichier supprimé avec succès"));
            logger.info("Fichier {} supprimé par utilisateur {}", fileId, userId);
            
        } catch (FileUploadException e) {
            logger.warn("Erreur lors de la suppression: {}", e.getMessage());
            JsonResponseUtil.sendError(response, e.getMessage(), 
                                     HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du fichier", e);
            JsonResponseUtil.sendError(response, "Erreur lors de la suppression du fichier", 
                                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
