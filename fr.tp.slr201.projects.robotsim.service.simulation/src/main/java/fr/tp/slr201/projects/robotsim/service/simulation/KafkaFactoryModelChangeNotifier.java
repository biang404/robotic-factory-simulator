package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

public class KafkaFactoryModelChangeNotifier implements FactoryModelChangedNotifier {
	
	private KafkaTemplate<String, Factory> simulationEventTemplate;
	private Factory factoryModel;
	private String topicName;
	
	public KafkaFactoryModelChangeNotifier(final Factory factoryModel,
										   final KafkaTemplate<String, Factory> simulationEventTemplate) {
		this.factoryModel = factoryModel;
		this.simulationEventTemplate = simulationEventTemplate;
		
	    String rawId = factoryModel.getId();
	    this.topicName = rawId.substring(rawId.lastIndexOf('/') + 1);
	}

	@Override
	public void notifyObservers() {
		final Message<Factory> factoryMessage = MessageBuilder.withPayload(factoryModel)
			    .setHeader(KafkaHeaders.TOPIC, "simulation-" + topicName)
			    .build();
		
		final CompletableFuture<SendResult<String, Factory>> sendResult =
			    simulationEventTemplate.send(factoryMessage);
		
		sendResult.whenComplete((result, ex) -> {
		    if (ex != null) {
		    	System.err.println("[KafkaNotifier] Failed to send event for topic "
                        + topicName + ": " + ex.getMessage());
		    }
		});
	}

    @Override
    public boolean addObserver(Object observer) {
    	return false;
    }

    @Override
    public boolean removeObserver(Object observer) {
        return false;
    }
}