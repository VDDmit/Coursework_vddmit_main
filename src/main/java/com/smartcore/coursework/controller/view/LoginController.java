package com.smartcore.coursework.controller.view;

import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final TemplateEngine templateEngine;

    @GetMapping("/login")
    @ResponseBody
    public String login(@RequestParam(value = "error", required = false) String error) {
        // Подготавливаем данные для шаблона
        Map<String, Object> model = new HashMap<>();
        if (error != null) {
            model.put("error", "Invalid username or password.");
        }

        // Создаем объект для вывода HTML
        TemplateOutput output = new StringOutput();

        // Рендерим шаблон с переданными данными
        templateEngine.render("login.jte", model, output);

        return output.toString();  // Возвращаем сгенерированный HTML
    }
}
