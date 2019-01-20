package com.glovoapp.backender.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {
    private final String welcomeMessage;

    RootController(@Value("${backender.welcome_message}") String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    @RequestMapping("/")
    @ResponseBody
    String root() {
        return welcomeMessage;
    }
}
