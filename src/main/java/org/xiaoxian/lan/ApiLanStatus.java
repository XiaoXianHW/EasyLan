package org.xiaoxian.lan;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xiaoxian.lan.ShareToLan.playerList;

public class ApiLanStatus {

    public static SimpleHttpServer server2;
    private static final Map<String, String> data = new HashMap<>();
    public static List<String> playerIDs = new ArrayList<>();

    public synchronized void start() throws IOException {
        if (server2 != null) {
            throw new IllegalStateException("HttpServer already started");
        }
        server2 = new SimpleHttpServer(28960);
        server2.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("HttpAPI Server started on port 28960");
    }

    public synchronized void stop() {
        if (server2 == null) {
            throw new IllegalStateException("Server not running");
        }
        server2.stop();
        server2 = null;
        System.out.println("Server stopped");
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    private static class SimpleHttpServer extends NanoHTTPD {

        public SimpleHttpServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            if ("/status".equals(uri)) {
                return handleStatus();
            } else if ("/playerlist".equals(uri)) {
                return handlePlayerList();
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
            }
        }

        private Response handleStatus() {
            Map<String, String> responseMap = new HashMap<>(data);
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(responseMap);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        }

        private Response handlePlayerList() {
            playerIDs.clear();
            for (EntityPlayerMP player : playerList) {
                playerIDs.add(player.getDisplayName());
            }
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(playerIDs);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        }
    }
}
