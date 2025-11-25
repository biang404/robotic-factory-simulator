package fr.tp.slr201.projects.robotsim.service.simulation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemotePersistenceConfig {

    @Bean
    public MicroServiceRemoteFactoryPersistenceManager remotePM(
            @Value("${sim.remote.host}") String host,
            @Value("${sim.remote.port}") int port) {
        return new MicroServiceRemoteFactoryPersistenceManager(host, port);
    }
}
