package com.smartcore.coursework.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectsController {
    
    @GetMapping("/projects")
    public String projects() {
        return "projects";
    }
}
