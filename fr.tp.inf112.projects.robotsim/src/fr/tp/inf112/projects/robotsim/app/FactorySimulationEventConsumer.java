package fr.tp.inf112.projects.robotsim.app;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import fr.tp.inf112.projects.robotsim.model.Factory;

public class FactorySimulationEventConsumer {
	
	private final KafkaConsumer<String, String> consumer;
	private final RemoteSimulatorController controller;
	private static final Logger LOGGER =
            Logger.getLogger(FactorySimulationEventConsumer.class.getName());
	
	public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
	    this.controller = controller;

	    final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

	    this.consumer = new KafkaConsumer<>(props);
	    Factory factory = (Factory) controller.getCanvas();
	    final String topicName = SimulationServiceUtils.getTopicName(factory);
	    this.consumer.subscribe(Collections.singletonList(topicName));
	}
	
	public void consumeMessages() {
	    try {
	        while (controller.isAnimationRunning()) {
	            final ConsumerRecords<String, String> records =
	                    consumer.poll(Duration.ofMillis(100));

	            for (final ConsumerRecord<String, String> record : records) {
	                String json = record.value();
	                LOGGER.fine("Received JSON Factory text '" + json + "'.");

	                try {
	                    Factory remoteFactory =
	                            controller.getObjectMapper().readValue(json, Factory.class);
	                    controller.setCanvas(remoteFactory);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                    LOGGER.warning("Failed to deserialize JSON: " + e.getMessage());
	                }
	            }
	        }
	    } finally {
	        consumer.close();
	    }
	}
}
