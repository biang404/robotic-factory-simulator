package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

public class RemoteSimulatorController extends SimulatorController {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    private final String serverHost;
    private final int serverPort;

    private volatile String modelId;

    private volatile boolean polling = false;
    private Thread pollingThread;

    private volatile Factory localFactory;

    public RemoteSimulatorController(Factory initialFactory,
                                     CanvasPersistenceManager pm,
                                     String serverHost,
                                     int serverPort) {
        super(initialFactory, pm);
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.mapper = createObjectMapper();
    }

    private static ObjectMapper createObjectMapper() {
        BasicPolymorphicTypeValidator ptv =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.shapes")
                        .allowIfSubType("fr.tp.inf112.projects.robotsim.model.path")
                        .allowIfSubType(BasicVertex.class.getPackageName())
                        .allowIfSubType(ArrayList.class.getName())
                        .allowIfSubType(LinkedHashSet.class.getName())
                        .allowIfSubType(LinkedHashMap.class.getName())
                        .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    @Override
    public void startAnimation() {
        if (modelId == null || modelId.isEmpty()) {
            return;
        }
        try {
            URI uri = new URI("http", null, serverHost, serverPort,
                    "/api/sim/start", "id=" + modelId, null);

            HttpRequest req = HttpRequest.newBuilder().uri(uri).GET().build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            startPollingState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopAnimation() {
        if (modelId == null || modelId.isEmpty()) {
            polling = false;
            return;
        }
        try {
            URI uri = new URI("http", null, serverHost, serverPort,
                    "/api/sim/stop", "id=" + modelId, null);

            HttpRequest req = HttpRequest.newBuilder().uri(uri).GET().build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        polling = false;
    }

    private Factory requestState() throws Exception {
        if (modelId == null || modelId.isEmpty()) {
            return null;
        }
        URI uri = new URI("http", null, serverHost, serverPort,
                "/api/sim/state", "id=" + modelId, null);

        HttpRequest req = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(resp.body(), Factory.class);
    }

    private void startPollingState() {
        if (pollingThread != null && pollingThread.isAlive()) return;

        polling = true;
        pollingThread = new Thread(() -> {
            while (polling) {
                try {
                    Factory remoteFactory = requestState();
                    Factory local = localFactory;
                    if (remoteFactory != null && local != null) {
                        syncFromRemote(local, remoteFactory);
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    polling = false;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void syncFromRemote(Factory local, Factory remote) {
        if (local == null || remote == null) {
            return;
        }

        java.util.List<fr.tp.inf112.projects.robotsim.model.Component> localComps = local.getAllComponents();
        java.util.List<fr.tp.inf112.projects.robotsim.model.Component> remoteComps = remote.getAllComponents();

        int n = Math.min(localComps.size(), remoteComps.size());

        for (int i = 0; i < n; i++) {
            fr.tp.inf112.projects.robotsim.model.Component lc = localComps.get(i);
            fr.tp.inf112.projects.robotsim.model.Component rc = remoteComps.get(i);

            lc.getPositionedShape().setxCoordinate(rc.getxCoordinate());
            lc.getPositionedShape().setyCoordinate(rc.getyCoordinate());
        }

        local.refreshCanvas();
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        super.setCanvas(canvasModel);
        if (canvasModel instanceof Factory) {
            this.localFactory = (Factory) canvasModel;
        } else {
            this.localFactory = null;
        }
    }
    
    @Override
    public boolean isAnimationRunning() {
        return polling;
    }
    
    Factory getLocalFactory() {
        return localFactory;
    }
    
    public void updateFromRemoteFactory(Factory remoteFactory) {
        Factory local = this.localFactory;
        if (local != null && remoteFactory != null) {
            syncFromRemote(local, remoteFactory);
        }
    }

}
