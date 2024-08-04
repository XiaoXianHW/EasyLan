package org.xiaoxian.lan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.util.ChatUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xiaoxian.EasyLan.*;
import static org.xiaoxian.lan.ApiLanStatus.server2;

public class ShareToLan {
    private final ApiLanStatus HttpApi = new ApiLanStatus();
    public static List<EntityPlayerMP> playerList;

    @SubscribeEvent
    public void onGuiButtonClick(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.gui instanceof GuiShareToLanEdit.GuiShareToLanModified) {
            handleLanSetup(event);
        } else if (event.gui instanceof GuiIngameMenu && event.button.id == 1 && HttpAPI) {
            stopHttpApi();
        }
    }

    private void handleLanSetup(GuiScreenEvent.ActionPerformedEvent event) {
        String fieldName = devMode ? "maxPlayers" : "field_72405_c";
        Minecraft mc = Minecraft.getMinecraft();
        IntegratedServer server = mc.getIntegratedServer();
        NetworkSystem networkSystem = MinecraftServer.getServer().func_147137_ag();

        configureLanPort(networkSystem);
        configureMaxPlayers(fieldName);

        if (HttpAPI) {
            startHttpApi(server);
        }

        scheduleApiUpdates(server);

        if (LanOutput) {
            displayLanInfo();
        }
    }

    private void configureLanPort(NetworkSystem networkSystem) {
        String portText = GuiShareToLanEdit.PortTextBox.getText();
        if (!portText.isEmpty()) {
            try {
                networkSystem.addLanEndpoint(InetAddress.getByName("0.0.0.0"), Integer.parseInt(portText));
                if (!LanOutput) {
                    ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPort") + " &f[&e" + portText + "&f]");
                }
            } catch (IOException e) {
                System.out.println("[EasyLan | networkSystem.addLanEndpoint] " + e.getMessage());
            }
        }
    }

    private void configureMaxPlayers(String fieldName) {
        String maxPlayerText = GuiShareToLanEdit.MaxPlayerBox.getText();
        if (!maxPlayerText.isEmpty()) {
            try {
                ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();
                Field maxplayerField = ServerConfigurationManager.class.getDeclaredField(fieldName);
                maxplayerField.setAccessible(true);
                maxplayerField.set(configManager, Integer.parseInt(maxPlayerText));
                if (!LanOutput) {
                    ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPlayer") + " &f[&e" + maxPlayerText + "&f]");
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.out.println("[EasyLan | ServerConfigurationManager.maxPlayers] " + e.getMessage());
            }
        }
    }

    private void startHttpApi(IntegratedServer server) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                System.out.println("[EasyLan | HttpApi] " + e.getMessage());
            }
            setupHttpApi(server);
            try {
                HttpApi.start();
            } catch (IOException e) {
                System.out.println("[EasyLan | HttpApi] " + e.getMessage());
            }
        });
    }

    private void setupHttpApi(IntegratedServer server) {
        String portText = GuiShareToLanEdit.PortTextBox.getText();
        HttpApi.set("port", portText.isEmpty() ? getLanPort() : portText);
        HttpApi.set("version", server.getMinecraftVersion());
        HttpApi.set("owner", server.getServerOwner());
        HttpApi.set("motd", server.getMOTD());
        HttpApi.set("pvp", String.valueOf(allowPVP));
        HttpApi.set("onlineMode", String.valueOf(onlineMode));
        HttpApi.set("spawnAnimals", String.valueOf(spawnAnimals));
        HttpApi.set("spawnNPCs", String.valueOf(spawnNPCs));
        HttpApi.set("allowFlight", String.valueOf(allowFlight));
        HttpApi.set("difficulty", String.valueOf(server.func_147135_j().getDifficultyResourceKey()));
        HttpApi.set("gameType", String.valueOf(server.getGameType()));
        HttpApi.set("maxPlayer", String.valueOf(server.getMaxPlayers()));
        HttpApi.set("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));
        updatePlayerList();
    }

    private void scheduleApiUpdates(IntegratedServer server) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            HttpApi.set("difficulty", String.valueOf(server.func_147135_j().getDifficultyResourceKey()));
            HttpApi.set("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));
            updatePlayerList();
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void updatePlayerList() {
        playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
        List<String> playerIDs = new ArrayList<>();
        for (EntityPlayerMP player : playerList) {
            playerIDs.add(player.getDisplayName());
        }
        ApiLanStatus.playerIDs = playerIDs;
    }

    private void displayLanInfo() {
        ExecutorService executor2 = Executors.newSingleThreadExecutor();
        executor2.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[EasyLan | displayLanInfo] " + e.getMessage());
            }
            String publicIp = getPublicIp();
            String localIp = getLocalIp();
            ChatUtil.sendMsg("&e[&6EasyLan&e] &aSuccessfully");
            ChatUtil.sendMsg("&4---------------------");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv4: &a" + localIp);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.public") + "IPv4: &a" + publicIp);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.chat.isPublic") + ": &a" + ("Unknown".equals(publicIp) ? "Unknown" : "Yes"));
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.port") + ": &a" + getLanPort());
            String portText = GuiShareToLanEdit.PortTextBox.getText();
            if (!portText.isEmpty()) {
                ChatUtil.sendMsg("&e" + I18n.format("easylan.text.CtPort") + ": &a" + portText);
            }
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.maxplayer") + ": &a" + MinecraftServer.getServer().getMaxPlayers());
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.onlineMode") + ": &a" + onlineMode);
            ChatUtil.sendMsg(" ");
            if (HttpAPI) {
                ChatUtil.sendMsg("&eHttp-Api:&a true");
                ChatUtil.sendMsg("&eApi-Status:&a localhost:28960/status");
                ChatUtil.sendMsg("&eApi-PlayerList:&a localhost:28960/playerlist");
            }
            ChatUtil.sendMsg("&4---------------------");
        });
    }

    private void stopHttpApi() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            if (server2 != null) {
                HttpApi.stop();
                System.out.println("HttpApi Stopped!");
            }
        });
    }

    private String getPublicIp() {
        try {
            URL url = new URL("https://easylan-api.xiaoxian.org/api/myipcheck");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(reader.readLine(), JsonObject.class);
                    return jsonObject.get("ip").getAsString();
                }
            }
        } catch (Exception e) {
            System.out.println("[EasyLan | getPublicIp] " + e.getMessage());
        }
        return "Unknown";
    }

    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            Pattern ipv4Pattern = Pattern.compile("^(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String hostAddress = address.getHostAddress();
                    Matcher matcher = ipv4Pattern.matcher(hostAddress);

                    if (matcher.matches() && !hostAddress.equals("127.0.0.1")) {
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("[EasyLan | getLocalIp] " + e.getMessage());
        }
        return "Unknown";
    }

    private String getLanPort() {
        return String.valueOf(MinecraftServer.getServer().getPort());
    }
}
