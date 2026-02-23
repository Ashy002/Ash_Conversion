package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.util.FlashMessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet pour la déconnexion des utilisateurs.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {RouteConstants.LOGOUT})
public class LogoutServlet extends BaseController {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Message de succès avant invalidation
            FlashMessageUtil.addSuccess(session, "Vous avez été déconnecté avec succès.");
            
            // Invalider la session
            session.invalidate();
            logger.debug("Session utilisateur invalidée");
        }
        
        // Rediriger vers la page de connexion
        redirectWithContext(request, response, RouteConstants.LOGIN);
    }
}

