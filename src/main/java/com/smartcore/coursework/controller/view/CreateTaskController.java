package com.smartcore.coursework.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CreateTaskController {

    @GetMapping("/create_task")
    public String createTasks() {
        return "create_task";
    }
}
