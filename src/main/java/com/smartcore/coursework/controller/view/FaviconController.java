package com.smartcore.coursework.controller.view;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {
    @GetMapping("/favicon.png")
    public void favicon() {
        //plug
    }
}
