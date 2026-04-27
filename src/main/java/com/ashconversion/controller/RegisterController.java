package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.constants.ViewConstants;
import com.ashconversion.exception.AuthenticationException;
import com.ashconversion.modele.dto.RegisterDTO;
import com.ashconversion.modele.entity.User;
import com.ashconversion.service.UserService;
import com.ashconversion.util.FlashMessageUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(RouteConstants.REGISTER)
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    /** Affiche la page d’inscription */
    @GetMapping
    public String showRegisterPage() {
        return ViewConstants.REGISTER;
    }

    /** Traite l’inscription */
    @PostMapping
    public String processRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session
    ) {

        try {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setConfirmPassword(confirmPassword);

            userService.register(dto);
           
            FlashMessageUtil.addSuccess(session,
                    "Inscription réussie ! Vous pouvez vous connecter.");

            return "redirect:" + RouteConstants.DASHBOARD;

        } catch (AuthenticationException e) {
            FlashMessageUtil.addError(session, e.getMessage());
            return "redirect:" + RouteConstants.REGISTER;
        }
    }
}
