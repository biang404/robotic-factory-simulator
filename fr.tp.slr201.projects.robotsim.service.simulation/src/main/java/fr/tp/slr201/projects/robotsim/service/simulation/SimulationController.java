package fr.tp.slr201.projects.robotsim.service.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

import fr.tp.inf112.projects.robotsim.model.Factory;

@RestController
@RequestMapping("/api/sim")
public class SimulationController {

    private final SimulationService service;
    @Autowired
    private KafkaTemplate<String, Factory> simulationEventTemplate;
    

    public SimulationController(SimulationService service) {
        this.service = service;
    }
    
    @GetMapping("/ping")
    public String ping() { return "ok"; }

    @RequestMapping(path = "/start", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> start(@RequestParam String id) throws Exception {
        boolean ok = service.start(id);
        if (ok) {
            Factory factory = service.get(id);

            if (factory != null) {
                FactoryModelChangedNotifier notifier =
                        new KafkaFactoryModelChangeNotifier(factory, simulationEventTemplate);
                factory.setNotifier(notifier);
            } else {
                System.err.println("[SimulationController] start(" + id + "): factory is null after start().");
            }
        }

        return ResponseEntity.ok().body("{\"started\":" + ok + "}");
    }

    @GetMapping("/state")
    public Factory state(@RequestParam String id) {
        return service.get(id);
    }

    @RequestMapping(path="/stop", method={RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> stop(@RequestParam String id) {
        boolean ok = service.stop(id);
        return ResponseEntity.ok().body("{\"stopped\":" + ok + "}");
    }
}
