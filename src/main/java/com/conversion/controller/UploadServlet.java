package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.exception.FileUploadException;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.service.FileService;
import com.Ash_Conversion.service.UserService;
import com.Ash_Conversion.util.FlashMessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet pour l'upload de fichiers.
 * Gère le multipart/form-data et crée un FileJob en DB.
 * 
 * Configuration multipart:
 * - maxFileSize: 100 MB
 * - maxRequestSize: 100 MB
 * - fileSizeThreshold: 1 MB (en mémoire avant écriture disque)
 */
@WebServlet(name = "UploadServlet", urlPatterns = {RouteConstants.API_FILES_UPLOAD})
@MultipartConfig(
    maxFileSize = 104857600L,      // 100 MB
    maxRequestSize = 104857600L,    // 100 MB
    fileSizeThreshold = 1048576     // 1 MB
)
public class UploadServlet extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);
    
    private final FileService fileService;
    private final UserService userService;
    
    public UploadServlet() {
        this.fileService = new FileService();
        this.userService = new UserService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier le token CSRF
        if (!validateCsrfToken(request, response)) {
            return; // Erreur déjà envoyée
        }
        
        // Vérifier l'authentification
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            redirectWithContext(request, response, RouteConstants.LOGIN);
            return;
        }
        
        // Récupérer l'utilisateur depuis la base de données
        User user = userService.findById(userId);
        if (user == null) {
            request.getSession().invalidate();
            redirectWithContext(request, response, RouteConstants.LOGIN);
            return;
        }
        
        HttpSession session = request.getSession();
        
        try {
            // Récupérer le fichier
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                FlashMessageUtil.addError(session, "Aucun fichier sélectionné");
                redirectWithContext(request, response, RouteConstants.DASHBOARD);
                return;
            }
            
            // Récupérer le type de conversion
            String conversionType = request.getParameter("conversionType");
            if (conversionType == null || conversionType.trim().isEmpty()) {
                FlashMessageUtil.addError(session, "Veuillez sélectionner un type de conversion");
                redirectWithContext(request, response, RouteConstants.DASHBOARD);
                return;
            }
            
            // Upload le fichier
            fileService.uploadFile(user, filePart, conversionType);
            
            // Message de succès
            String filename = filePart.getSubmittedFileName();
            FlashMessageUtil.addSuccess(session, 
                String.format("Fichier '%s' uploadé avec succès !", filename));
            
            logger.info("Upload réussi: {} par {}", filename, user.getUsername());
            
            // Rediriger vers le dashboard
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
            
        } catch (FileUploadException e) {
            // Erreur de validation ou d'upload
            logger.warn("Erreur lors de l'upload: {}", e.getMessage());
            FlashMessageUtil.addError(session, e.getMessage());
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
            
        } catch (Exception e) {
            // Erreur inattendue
            logger.error("Erreur inattendue lors de l'upload", e);
            FlashMessageUtil.addError(session, 
                "Une erreur est survenue lors de l'upload. Veuillez réessayer.");
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
        }
    }
}

