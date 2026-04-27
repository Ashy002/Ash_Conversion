package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.exception.FileUploadException;
import com.ashconversion.modele.entity.User;
import com.ashconversion.service.FileService;
import com.ashconversion.service.UserService;
import com.ashconversion.util.FlashMessageUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(RouteConstants.API_FILES_UPLOAD)
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private final FileService fileService;
    private final UserService userService;

    public UploadController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @PostMapping
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversionType") String conversionType,
            HttpSession session
    ) {
        // Vérification de l'authentification
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:" + RouteConstants.LOGIN;
        }

        User user = userService.findById(userId);
        if (user == null) {
            session.invalidate();
            return "redirect:" + RouteConstants.LOGIN;
        }

        try {
            // Validation du fichier
            if (file == null || file.isEmpty()) {
                FlashMessageUtil.addError(session, "Aucun fichier sélectionné");
                return "redirect:" + RouteConstants.DASHBOARD;
            }

            // Validation du type de conversion
            if (conversionType == null || conversionType.trim().isEmpty()) {
                FlashMessageUtil.addError(session, "Veuillez sélectionner un type de conversion");
                return "redirect:" + RouteConstants.DASHBOARD;
            }

             // Appel du service pour l'upload
           fileService.uploadFile(user, file, conversionType);

            FlashMessageUtil.addSuccess(session,
                    String.format("Fichier '%s' uploadé avec succès !", file.getOriginalFilename()));

            logger.info("Upload réussi: {} par {}", file.getOriginalFilename(), user.getUsername());

            return "redirect:" + RouteConstants.DASHBOARD;

        } catch (FileUploadException e) {
            logger.warn("Erreur lors de l'upload: {}", e.getMessage());
            FlashMessageUtil.addError(session, e.getMessage());
            return "redirect:" + RouteConstants.DASHBOARD;

        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'upload", e);
            FlashMessageUtil.addError(session,
                    "Une erreur est survenue lors de l'upload. Veuillez réessayer.");
            return "redirect:" + RouteConstants.DASHBOARD;
        }
    }
}
