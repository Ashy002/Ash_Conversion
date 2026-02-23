package com.Ash_Conversion.constants;

/**
 * Constantes pour les routes de l'application.
 * Élimine les magic strings et facilite la maintenance.
 */
public final class RouteConstants {
    
    private RouteConstants() {
        // Classe utilitaire, pas d'instanciation
    }
    
    // Auth routes
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String LOGOUT = "/logout";
    
    // Dashboard routes
    public static final String DASHBOARD = "/dashboard";
    
    // API routes
    public static final String API_FILES = "/api/files";
    public static final String API_FILES_UPLOAD = "/api/files/upload";
    public static final String API_CONVERT = "/api/convert";
    public static final String API_DOWNLOAD = "/api/download";
    public static final String API_PREVIEW = "/api/preview";
    public static final String API_SHARE = "/api/share";
    public static final String SHARE_PUBLIC = "/share";
}

