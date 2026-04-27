package com.ashconversion.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour générer des réponses JSON standardisées.
 */
public final class JsonResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    private JsonResponseUtil() {}

    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("success", true);
        if (data != null) jsonResponse.put("data", data);
        sendJson(response, jsonResponse);
    }

    public static void sendError(HttpServletResponse response,
                                 String message,
                                 int statusCode) throws IOException {

        response.setStatus(statusCode);

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("success", false);
        jsonResponse.put("error", message);

        sendJson(response, jsonResponse);
    }

    public static void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }
}
