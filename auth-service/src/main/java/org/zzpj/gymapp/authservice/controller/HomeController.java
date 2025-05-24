package org.zzpj.gymapp.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home(Authentication authentication, HttpServletRequest request) {
        String username = authentication != null ? authentication.getName() : "Guest";
        
        return "<html><body>" +
                "<h1>Welcome to GymApp Authentication Service</h1>" +
                "<p>Hello, " + username + "!</p>" +
                "<p>You have been authenticated. If you were in an OAuth2 flow, " +
                "you may need to restart the authorization process.</p>" +
                "</body></html>";
    }
} 