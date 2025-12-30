package com.kiran.musicapi;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {

	static final DockerImageName POSTGRESDB_IMAGE = DockerImageName.parse("postgres:16.10")
			.asCompatibleSubstituteFor("postgres");

	@SuppressWarnings("resource")
	public PostgresContainer() {
		super(POSTGRESDB_IMAGE);
		withUsername("testuser");
		withPassword("testpass");
		setLogging();
		setShutDown();
	}

	private void setShutDown() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down Postgres container");
			this.stop();
			log.info("Container shut down");
		}));
	}

	@SuppressWarnings("resource")
	private void setLogging() {
		withLogConsumer(outputFrame -> {
			log.info("[Postgres Container] {}", outputFrame.getUtf8String().trim());
		});
	}
}
