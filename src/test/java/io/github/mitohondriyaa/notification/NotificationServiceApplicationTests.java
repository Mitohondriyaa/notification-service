package io.github.mitohondriyaa.notification;

import io.github.mitohondriyaa.order.event.OrderPlacedEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NotificationServiceApplicationTests {
	static Network network = Network.newNetwork();
	@ServiceConnection
	static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0")
		.withListener("kafka:19092")
		.withNetwork(network)
		.withNetworkAliases("kafka");
	@SuppressWarnings("resource")
	static GenericContainer<?> schemaRegistryContainer = new GenericContainer<>("confluentinc/cp-schema-registry:7.4.0")
		.withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
		.withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
		.withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
		.withExposedPorts(8081)
		.withNetwork(network)
		.withNetworkAliases("schema-registry")
		.waitingFor(Wait.forHttp("/subjects"));

	static {
		kafkaContainer.start();
		schemaRegistryContainer.start();
	}

	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.producer.properties.schema.registry.url",
			() -> "http://localhost:" + schemaRegistryContainer.getMappedPort(8081));
		registry.add("spring.kafka.consumer.properties.schema.registry.url",
			() -> "http://localhost:" + schemaRegistryContainer.getMappedPort(8081));
	}

	KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
	@MockitoSpyBean
	JavaMailSender mailSender;

	NotificationServiceApplicationTests(
		KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
		KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate
	) {
		this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
		this.kafkaTemplate = kafkaTemplate;
	}

	@BeforeEach
	void setUp() {
		kafkaListenerEndpointRegistry.getAllListenerContainers()
			.forEach(Lifecycle::start);
	}

	@Test
	void shouldSendNotification() {
		OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
		orderPlacedEvent.setOrderNumber(UUID.randomUUID().toString());
		orderPlacedEvent.setEmail("test@example.com");
		orderPlacedEvent.setFirstName("Oleg");
		orderPlacedEvent.setLastName("Kireev");

		kafkaTemplate.sendDefault(orderPlacedEvent);

		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() ->
				verify(mailSender).send(any(MimeMessagePreparator.class))
			);
	}

	@AfterEach
	void tearDown() {
		kafkaListenerEndpointRegistry.getAllListenerContainers()
			.forEach(Lifecycle::stop);
	}

	@AfterAll
	static void stopContainers() {
		kafkaContainer.stop();
		schemaRegistryContainer.stop();
		network.close();
	}
}
