package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.tp.inf112.projects.robotsim.model.Factory;

@Service
public class SimulationService {
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final RemoteFactoryPersistenceManagerNoCanvas remotePM;
    private final Map<String, Factory> running = new ConcurrentHashMap<>();

    public SimulationService(RemoteFactoryPersistenceManagerNoCanvas remotePM) {
        this.remotePM = remotePM;
    }

    public boolean start(String id) throws Exception {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (running.containsKey(id)) {
            log.info("[start] Model '{}' already running. Skip re-start.", id);
            return true;
        }

        log.info("[start] Loading model '{}' from remote server...", id);
        long t0 = System.currentTimeMillis();
        Factory factory = remotePM.readFactory(id);
        long t1 = System.currentTimeMillis();

        running.put(id, factory);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            org.slf4j.LoggerFactory.getLogger(SimulationService.class)
                .error("[thread] Uncaught exception in {}: {}", t.getName(), e.toString(), e);
        });
        factory.startSimulation();
        log.info("[start] Model '{}' started. loadTime={}ms, runningCount={}", id, (t1 - t0), running.size());
        return true;
    }

    public Factory get(String id) {
        Factory f = running.get(id);
        if (f == null) {
            log.warn("[get] No running simulation for '{}'", id);
            throw new IllegalArgumentException("No running simulation for " + id);
        }
        log.debug("[get] Returning model '{}' snapshot.", id);
        return f;
    }

    public boolean stop(String id) {
        Factory f = running.remove(id);
        if (f == null) {
            log.warn("[stop] Model '{}' not found in running map.", id);
            return false;
        }
        f.stopSimulation();
        log.info("[stop] Model '{}' stopped. runningCount={}", id, running.size());
        return true;
    }
}

