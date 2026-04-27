package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.constants.ViewConstants;
import com.ashconversion.exception.AuthenticationException;
import com.ashconversion.modele.dto.LoginDTO;
import com.ashconversion.modele.entity.User;
import com.ashconversion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller Spring pour la connexion des utilisateurs.
 * Migration du LoginServlet Java EE vers Spring MVC.
 */
@Controller
@RequestMapping(RouteConstants.LOGIN)
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Affiche la page de connexion.
     */
    @GetMapping
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute(SESSION_USER_ID_ATTRIBUTE) != null) {
            return "redirect:" + RouteConstants.DASHBOARD;
        }
        return ViewConstants.LOGIN;
    }

    /**
     * Traite la soumission du formulaire de connexion.
     */
    @PostMapping
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(username);
            loginDTO.setPassword(password);

            User user = userService.login(loginDTO);

            // Protection contre la fixation de session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(SESSION_USER_ID_ATTRIBUTE, user.getId());
            newSession.setMaxInactiveInterval(30 * 60);

            redirectAttributes.addFlashAttribute("success",
                    "Connexion réussie. Bienvenue " + user.getUsername());

            logger.info("Utilisateur connecté : {}", user.getUsername());
            return "redirect:" + RouteConstants.DASHBOARD;

        } catch (AuthenticationException e) {
            logger.warn("Échec de connexion pour l'utilisateur {}", username);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + RouteConstants.LOGIN;
        }
    }
}
