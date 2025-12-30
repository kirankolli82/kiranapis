package com.kiran.musicapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest
@Testcontainers
public class HappyFlowTest {

	@Container
	static KafkaContainer KAFKA_CONTAINER = new KafkaContainer();

	@Container
	static PostgresContainer POSTGRESDB_CONTAINER = new PostgresContainer();

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
		registry.add("spring.datasource.url", POSTGRESDB_CONTAINER::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRESDB_CONTAINER::getUsername);
		registry.add("spring.datasource.password", POSTGRESDB_CONTAINER::getPassword);
	}

	@Test
	public void testHappy() {
		Assertions.assertTrue(true);
	}
}
