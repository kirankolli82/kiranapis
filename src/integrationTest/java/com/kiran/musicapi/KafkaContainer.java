package com.kiran.musicapi;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
@Slf4j
public class KafkaContainer extends org.testcontainers.kafka.KafkaContainer {
	static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:8.1.0")
			.asCompatibleSubstituteFor("apache/kafka");

	public KafkaContainer() {
		super(KAFKA_IMAGE);
		setWaitStrategy();
		setLogging();
		setupEnv();
		setCommand();
		setShutDown();
	}

	private void setShutDown() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down kafka container");
			this.stop();
			log.info("Container shut down");
		}));
	}

	private void setLogging() {
		withLogConsumer(outputFrame -> {
			log.info("[Kafka Container] {}", outputFrame.getUtf8String().trim());
		});
	}

	private void setCommand() {
		withCommand("bash", "-c", "mkdir -p /var/lib/kafka/data /var/lib/kafka/metadata && "
				+ "if [ ! -f /var/lib/kafka/metadata/meta.properties ]; then "
				+ "echo 'Initializing KRaft metadata...' && "
				+ "kafka-storage format --config /etc/kafka/server.properties --cluster-id 'test-cluster-id' --standalone; "
				+ "fi && " + "/etc/confluent/docker/run");
	}

	private void setWaitStrategy() {
		var waitStrategy = super.getWaitStrategy().withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
		waitingFor(waitStrategy);
	}

	private void setupEnv() {
		Map<String, String> env = new HashMap<>();
		env.put("KAFKA_PROCESS_ROLES", "broker,controller");
		env.put("KAFKA_NODE_ID", "1");
		env.put("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:29093");
		env.put("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
				"CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT");
		env.put("KAFKA_LISTENERS",
				"PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093");
		env.put("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:29092,PLAINTEXT_HOST://localhost:9092");
		env.put("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");
		env.put("KAFKA_CONTROLLER_LISTENER_NAME", "CONTROLLER");
		env.put("KAFKA_OFFSET_TOPIC_REPLICATION_FACTOR", "1");
		env.put("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");
		env.put("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");
		env.put("KAFKA_MIN_INSYNC_REPLICAS", "1");
		env.put("KAFKA_LOG_DIRS", "/var/lib/kafka/data");
		env.put("KAFKA_METADATA_LOG_DIR", "/var/lib/kafka/metadata");
		env.put("CLUSTER_ID", "test-cluster-id");
		withEnv(env);
	}
}
