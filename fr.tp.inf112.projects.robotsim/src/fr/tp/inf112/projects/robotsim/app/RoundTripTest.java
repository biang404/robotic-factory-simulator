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
                        // 你的模型类
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.shapes")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.path")
                        // BasicVertex 所在的包
                        .allowIfSubType(BasicVertex.class.getPackageName())
                        // 常见集合类
                        .allowIfSubType(ArrayList.class.getName())
                        .allowIfSubType(LinkedHashSet.class.getName())
                        .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    public static void main(String[] args) throws Exception {
        // 1. 用和 GUI 一样的逻辑创建工厂
        Factory original = SimulatorApplication.buildFactory();

        // 2. 创建 mapper
        ObjectMapper mapper = createObjectMapper();

        // 3. 序列化
        String json = mapper.writeValueAsString(original);
        System.out.println("===== JSON =====");
        System.out.println(json);

        // 4. 反序列化
        Factory restored = mapper.readValue(json, Factory.class);
        System.out.println("===== RESTORED FACTORY =====");
        System.out.println(restored);

        // 5. 简单 sanity check：启动一下仿真（不用 GUI，只要不抛异常）
        restored.startSimulation();
        Thread.sleep(200);      // 让线程跑一会
        restored.stopSimulation();

        System.out.println(">>> ROUND-TRIP OK，模型可以正常序列化/反序列化。");
    }
}