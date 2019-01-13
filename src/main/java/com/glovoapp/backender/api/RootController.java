package com.glovoapp.backender.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class RootController {
    private final String welcomeMessage;

    RootController(@Value("${backender.welcome_message}") String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public static void main(String[] args) {
        SpringApplication.run(RootController.class);
    }

    @RequestMapping("/")
    @ResponseBody
    String root() {
        return welcomeMessage;
    }
}
