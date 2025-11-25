package fr.tp.inf112.projects.robotsim.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class RoundTripTest {

    private static ObjectMapper createObjectMapper() {
        BasicPolymorphicTypeValidator ptv =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.shapes")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.path")
                        .allowIfSubType(BasicVertex.class.getPackageName())
                        .allowIfSubType(ArrayList.class.getName())
                        .allowIfSubType(LinkedHashSet.class.getName())
                        .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    public static void main(String[] args) throws Exception {
        Factory original = SimulatorApplication.buildFactory();

        ObjectMapper mapper = createObjectMapper();

        String json = mapper.writeValueAsString(original);
        System.out.println("===== JSON =====");
        System.out.println(json);

        Factory restored = mapper.readValue(json, Factory.class);
        System.out.println("===== RESTORED FACTORY =====");
        System.out.println(restored);

        restored.startSimulation();
        Thread.sleep(200);
        restored.stopSimulation();

        System.out.println(">>> ROUND-TRIP OK，the model can be correctly serialized/deserialized.");
    }
}