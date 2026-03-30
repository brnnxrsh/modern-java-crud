package com.brenner.modern_java_crud;

import java.io.File;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerComposeContainer<?> COMPOSE = startCompose();

    private static DockerComposeContainer<?> startCompose() {
        final DockerComposeContainer<?> compose = new DockerComposeContainer<>(
            new File("compose.test.yml")
        );
        compose.withExposedService("postgres", 5432, Wait.forListeningPort());
        compose.withExposedService(
            "member-api-mock",
            1080,
            Wait.forHttp("/members/1").forStatusCode(200)
        );

        compose.start();
        Runtime.getRuntime().addShutdownHook(new Thread(compose::stop));
        return compose;
    }

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("DB_HOST", () -> COMPOSE.getServiceHost("postgres", 5432));
        registry.add(
            "DB_PORT",
            () -> String.valueOf(COMPOSE.getServicePort("postgres", 5432))
        );
        registry.add("DB_NAME", () -> "db-test");
        registry.add("DB_USER", () -> "admin");
        registry.add("DB_PASS", () -> "admin");
        registry.add(
            "clients.member.url",
            () -> String.format(
                "http://%s:%d",
                COMPOSE.getServiceHost("member-api-mock", 1080),
                COMPOSE.getServicePort("member-api-mock", 1080)
            )
        );
    }

}
