package fr.tp.slr201.projects.robotsim.service.simulation;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class EntryApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntryApplication.class, args);
    }
    
    @Bean
    ApplicationRunner showMappings(RequestMappingHandlerMapping mapping) {
      return args -> mapping.getHandlerMethods().forEach((info, method) ->
          System.out.println("MAPPED: " + info));
    }
}