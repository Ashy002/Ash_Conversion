package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.exception.FileUploadException;
import com.ashconversion.modele.entity.User;
import com.ashconversion.service.FileService;
import com.ashconversion.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API pour supprimer un fichier
 * Endpoint : DELETE /api/files/{id}
 */
@RestController
@RequestMapping(RouteConstants.API_FILES)
public class FileDeleteController {

    private static final Logger logger = LoggerFactory.getLogger(FileDeleteController.class);

    private final FileService fileService;
    private final UserService userService;

    public FileDeleteController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    /**
     * Supprime un fichier appartenant à l'utilisateur connecté
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Utilisateur non authentifié"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Utilisateur introuvable"));
        }

        try {
            fileService.deleteFile(fileId, user);
            logger.info("Fichier {} supprimé par utilisateur {}", fileId, userId);

            return ResponseEntity.ok(
                    Map.of("message", "Fichier supprimé avec succès")
            );

        } catch (FileUploadException e) {
            logger.warn("Erreur suppression fichier: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur suppression fichier", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la suppression du fichier"));
        }
    }
}
