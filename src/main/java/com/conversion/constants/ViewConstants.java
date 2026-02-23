package com.Ash_Conversion.constants;

/**
 * Constantes pour les chemins des vues JSP.
 * Élimine les magic strings et facilite la maintenance.
 */
public final class ViewConstants {
    
    private ViewConstants() {
        // Classe utilitaire, pas d'instanciation
    }
    
    // Base path
    private static final String VIEWS_BASE = "/WEB-INF/views";
    
    // Auth views
    public static final String LOGIN_VIEW = VIEWS_BASE + "/auth/login.jsp";
    public static final String REGISTER_VIEW = VIEWS_BASE + "/auth/register.jsp";
    
    // Dashboard views
    public static final String DASHBOARD_VIEW = VIEWS_BASE + "/dashboard/dashboard.jsp";
    
    // Error views
    public static final String ERROR_VIEW = VIEWS_BASE + "/shared/error.jsp";
}

