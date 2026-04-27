package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.constants.ViewConstants;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.User;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.service.FileJobService;
import com.ashconversion.service.UserService;
import com.ashconversion.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(RouteConstants.DASHBOARD)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final FileJobService fileJobService;
    private final UserService userService;

    public DashboardController(FileJobService fileJobService, UserService userService) {
        this.fileJobService = fileJobService;
        this.userService = userService;
    }

    @GetMapping
    public String showDashboard(
            @RequestParam(value = "tab", required = false) String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) String pageParam,
            HttpSession session,
            Model model
    ) {
        try {
            // Récupérer l'utilisateur depuis la session
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:" + RouteConstants.LOGIN;
            }

            User user = userService.findById(userId);
            if (user == null) {
                session.invalidate();
                return "redirect:" + RouteConstants.LOGIN;
            }

            // Validation onglet
            if (tab != null && !tab.matches("^(all|uploaded|converted)$")) {
                logger.warn("Onglet invalide: {}", tab);
                tab = "all";
            }

            // Validation terme de recherche
            if (search != null && !ValidationUtil.isValidSearchTerm(search)) {
                logger.warn("Terme de recherche invalide: {}", search);
                search = null;
            }

            // Validation et parsing page
            int page = 0;
            if (pageParam != null && !pageParam.isEmpty()) {
                if (!ValidationUtil.isValidPageNumber(pageParam)) {
                    logger.warn("Numéro de page invalide: {}", pageParam);
                    page = 0;
                } else {
                    try {
                        page = Integer.parseInt(pageParam) - 1;
                        if (page < 0) page = 0;
                    } catch (NumberFormatException e) {
                        logger.warn("Erreur lors du parsing de la page: {}", pageParam);
                        page = 0;
                    }
                }
            }

            // Déterminer le statut selon l'onglet
            ConversionStatus status = null;
            if ("converted".equals(tab)) {
                status = ConversionStatus.COMPLETED;
            } else if ("uploaded".equals(tab)) {
                status = ConversionStatus.UPLOADED;
            }

            // Récupérer les statistiques
            Map<String, Long> stats = fileJobService.getStats(user);

            // Récupérer la liste paginée des fichiers
            List<FileJob> fileJobs = fileJobService.getFileJobs(user, status, search, page, DEFAULT_PAGE_SIZE);

            // Pagination
            long totalCount = fileJobService.countFileJobs(user, status, search);
            int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);

            // Ajouter les attributs au model pour JSP
            model.addAttribute("stats", stats);
            model.addAttribute("fileJobs", fileJobs);
            model.addAttribute("currentTab", tab != null ? tab : "all");
            model.addAttribute("currentSearch", search != null ? search : "");
            model.addAttribute("currentPage", page + 1);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("pageSize", DEFAULT_PAGE_SIZE);
            
            // Enums de statut pour JSP
            model.addAttribute("UPLOADED", ConversionStatus.UPLOADED);
            model.addAttribute("PROCESSING", ConversionStatus.PROCESSING);
            model.addAttribute("COMPLETED", ConversionStatus.COMPLETED);
           model.addAttribute("FAILED", ConversionStatus.FAILED);

            
            // les enums pour le JSP
            model.addAttribute("PDF_TO_WORD", "PDF_TO_WORD");
            model.addAttribute("WORD_TO_PDF", "WORD_TO_PDF");
            model.addAttribute("PDF_TO_EXCEL", "PDF_TO_EXCEL");
            model.addAttribute("EXCEL_TO_PDF", "EXCEL_TO_PDF");

            return ViewConstants.DASHBOARD;

        } catch (Exception e) {
            logger.error("Erreur lors du chargement du dashboard", e);
            model.addAttribute("errorMessage", "Erreur lors du chargement du dashboard");
            return ViewConstants.ERROR;
        }
    }
}

