package com.brenner.modern_java_crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ModernJavaCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModernJavaCrudApplication.class, args);
    }

}
