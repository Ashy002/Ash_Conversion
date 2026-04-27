package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.util.FlashMessageUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(RouteConstants.LOGOUT)
public class LogoutController {

    @GetMapping
    public String logoutGet(HttpSession session) {
        return logout(session);
    }

    @PostMapping
    public String logout(HttpSession session) {
        FlashMessageUtil.addSuccess(session, "Vous avez été déconnecté avec succès.");
        session.invalidate();
        return "redirect:" + RouteConstants.LOGIN;
    }
}