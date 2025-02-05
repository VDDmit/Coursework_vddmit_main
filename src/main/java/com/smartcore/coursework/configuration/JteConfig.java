package com.smartcore.coursework.configuration;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class JteConfig {

    @Bean
    public TemplateEngine templateEngine() {
        return TemplateEngine.create(new DirectoryCodeResolver(Paths.get("src/main/jte")), ContentType.Html);
    }
}
