package com.brenner.modern_java_crud;

import org.springframework.boot.SpringApplication;

public class TestModernJavaCrudApplication {

    public static void main(String[] args) {
        SpringApplication.from(ModernJavaCrudApplication::main)
            .with(TestcontainersConfiguration.class)
            .run(args);
    }

}
