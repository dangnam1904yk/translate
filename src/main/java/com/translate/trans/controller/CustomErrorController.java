package com.translate.trans.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        int statusCode = (int) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        model.addAttribute("status", statusCode);
        model.addAttribute("error", "Lỗi hệ thống");
        model.addAttribute("message", errorMessage);
        model.addAttribute("path", request.getAttribute("javax.servlet.error.request_uri"));

        return "error"; // Trả về trang error.html
    }
}
