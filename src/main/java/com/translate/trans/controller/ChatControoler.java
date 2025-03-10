package com.translate.trans.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatControoler {

    @GetMapping("/chat")
    public String getMethodName() {
        return "chat";
    }

}
