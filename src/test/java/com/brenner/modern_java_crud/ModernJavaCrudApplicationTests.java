package com.brenner.modern_java_crud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ModernJavaCrudApplicationTests {

	@Test
	void contextLoads() {
	}

}
