package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.service.FileJobService;
import com.Ash_Conversion.util.FileStreamUtil;
import com.Ash_Conversion.util.MimeTypeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Servlet pour prévisualiser les fichiers convertis.
 * Endpoint: /preview?id=...
 * Pour les PDF: affiche dans un iframe
 * Pour les autres: propose le téléchargement ou un message
 */
@WebServlet(name = "PreviewServlet", urlPatterns = {RouteConstants.API_PREVIEW})
public class PreviewServlet extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(PreviewServlet.class);
    private final FileJobService fileJobService = new FileJobService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long userId = requireAuthenticatedUser(request, response);
        if (userId == null) {
            return; // Erreur déjà envoyée
        }
        
        Long fileId = parseFileId(request, response);
        if (fileId == null) {
            return; // Erreur déjà envoyée
        }
        
        try {
            // Récupérer le FileJob et vérifier la propriété
            FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
            if (fileJob == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier introuvable ou accès non autorisé");
                return;
            }
            
            // Vérifier que le fichier est converti
            if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Le fichier n'est pas encore converti (statut: " + fileJob.getStatus() + ")");
                return;
            }
            
            // Vérifier que le fichier existe
            File outputFile = new File(fileJob.getOutputPath());
            if (!outputFile.exists()) {
                logger.error("Fichier converti introuvable: {}", fileJob.getOutputPath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier converti introuvable");
                return;
            }
            
            // Déterminer si on peut prévisualiser (PDF uniquement)
            String filename = fileJob.getOutputFilename().toLowerCase();
            boolean isPdf = filename.endsWith(".pdf");
            
            if (!isPdf) {
                // Pour Word/Excel, retourner un message JSON
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"previewable\": false, \"message\": \"La prévisualisation n'est disponible que pour les fichiers PDF. Veuillez télécharger le fichier.\", \"downloadUrl\": \"/api/download?id=" + fileId + "\"}");
                return;
            }
            
            // Stream le fichier PDF en mode inline
            String mimeType = MimeTypeUtil.getMimeType(fileJob.getOutputFilename());
            FileStreamUtil.streamFile(outputFile, response, mimeType, 
                                     fileJob.getOutputFilename(), true);
            
            logger.info("Fichier prévisualisé: {} par utilisateur: {}", 
                       fileJob.getOutputFilename(), userId);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la prévisualisation", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                              "Erreur lors de la prévisualisation");
        }
    }
}

