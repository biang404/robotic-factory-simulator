package fr.tp.slr201.projects.robotsim.service.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        BasicPolymorphicTypeValidator ptv =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.shapes")
                        .allowIfSubType("java.util")
                        .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.findAndRegisterModules();
        return mapper;
    }
}

