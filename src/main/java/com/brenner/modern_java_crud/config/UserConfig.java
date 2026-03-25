package com.brenner.modern_java_crud.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record UserConfig(
    List<User> users
) {

    public record User(
        String name,
        String password,
        String role
    ) {}

}
