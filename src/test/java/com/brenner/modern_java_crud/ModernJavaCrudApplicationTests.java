package com.brenner.modern_java_crud;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class ModernJavaCrudApplicationTests extends TestcontainersConfiguration {

    @Test
    void contextLoads() {}

}
