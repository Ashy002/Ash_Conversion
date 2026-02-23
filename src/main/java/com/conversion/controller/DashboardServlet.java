package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.constants.ViewConstants;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.service.FileJobService;
import com.Ash_Conversion.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Servlet pour le dashboard.
 * Charge les statistiques et la liste des fichiers avec filtres et pagination.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {RouteConstants.DASHBOARD})
public class DashboardServlet extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    private final FileJobService fileJobService;
    private final UserService userService;
    
    public DashboardServlet() {
        this.fileJobService = new FileJobService();
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Récupérer l'ID utilisateur de la session
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                redirectWithContext(request, response, RouteConstants.LOGIN);
                return;
            }
            
            // Récupérer l'utilisateur depuis la base de données
            User user = userService.findById(userId);
            if (user == null) {
                // Utilisateur supprimé ou session invalide
                request.getSession().invalidate();
                redirectWithContext(request, response, RouteConstants.LOGIN);
                return;
            }
            
            // Récupérer et valider les paramètres de filtrage et pagination
            String tab = request.getParameter("tab"); // all, uploaded, converted
            String search = request.getParameter("search"); // terme de recherche
            String pageParam = request.getParameter("page");
            
            // Valider le paramètre tab
            if (tab != null && !tab.matches("^(all|uploaded|converted)$")) {
                logger.warn("Onglet invalide: {}", tab);
                tab = "all";
            }
            
            // Valider le terme de recherche
            if (search != null && !com.Ash_Conversion.util.ValidationUtil.isValidSearchTerm(search)) {
                logger.warn("Terme de recherche invalide: {}", search);
                search = null;
            }
            
            // Valider et parser le numéro de page
            int page = 0;
            if (pageParam != null && !pageParam.isEmpty()) {
                if (!com.Ash_Conversion.util.ValidationUtil.isValidPageNumber(pageParam)) {
                    logger.warn("Numéro de page invalide: {}", pageParam);
                    page = 0;
                } else {
                    try {
                        page = Integer.parseInt(pageParam) - 1; // Convertir en 0-based
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
            List<com.Ash_Conversion.model.entity.FileJob> fileJobs = 
                fileJobService.getFileJobs(user, status, search, page, DEFAULT_PAGE_SIZE);
            
            // Compter le total pour la pagination
            long totalCount = fileJobService.countFileJobs(user, status, search);
            int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);
            
            // Ajouter les attributs à la requête
            request.setAttribute("stats", stats);
            request.setAttribute("fileJobs", fileJobs);
            request.setAttribute("currentTab", tab != null ? tab : "all");
            request.setAttribute("currentSearch", search != null ? search : "");
            request.setAttribute("currentPage", page + 1); // 1-based pour l'affichage
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("pageSize", DEFAULT_PAGE_SIZE);
            
            // Forward vers la vue
            forwardToDashboard(request, response);
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du dashboard", e);
            handleError(request, response, e, "Erreur lors du chargement du dashboard");
        }
    }
}


