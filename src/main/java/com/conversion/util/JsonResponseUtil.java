package com.Ash_Conversion.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour générer des réponses JSON standardisées.
 * Élimine la duplication de code dans les controllers.
 */
public class JsonResponseUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    
    /**
     * Envoie une réponse JSON de succès.
     */
    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("success", true);
        if (data != null) {
            jsonResponse.put("data", data);
        }
        sendJson(response, jsonResponse);
    }
    
    /**
     * Envoie une réponse JSON d'erreur.
     */
    public static void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("success", false);
        jsonResponse.put("error", message);
        sendJson(response, jsonResponse);
    }
    
    /**
     * Envoie une réponse JSON avec un objet personnalisé.
     */
    public static void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }
    
    /**
     * Envoie une réponse JSON vide (tableau vide).
     */
    public static void sendEmptyArray(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("[]");
    }
    
    /**
     * Envoie une réponse JSON vide (objet vide).
     */
    public static void sendEmptyObject(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{}");
    }
}

