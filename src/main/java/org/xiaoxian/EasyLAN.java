package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;
import org.xiaoxian.lan.ServerStarting;
import org.xiaoxian.lan.ServerStopping;
import org.xiaoxian.util.ConfigUtil;

@Mod(EasyLAN.MOD_ID)
public class EasyLAN {

    public static final String MOD_ID = "easylan";

    public static boolean allowPVP = true;
    public static boolean onlineMode = true;
    public static boolean spawnAnimals = true;
    public static boolean allowFlight = true;
    public static boolean whiteList = false;
    public static boolean BanCommands = false;
    public static boolean OpCommands = false;
    public static boolean SaveCommands = false;
    public static boolean HttpAPI = true;
    public static boolean LanOutput = true;
    public static String CustomPort = "25565";
    public static String CustomMaxPlayer = "20";
    public static String motd = "This is a Default EasyLAN Motd!";

    public EasyLAN() {
        ConfigUtil.load();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());
        MinecraftForge.EVENT_BUS.register(new ServerStarting());
        MinecraftForge.EVENT_BUS.register(new ServerStopping());

        GuiShareToLanEdit.PortText = CustomPort;
        GuiShareToLanEdit.MaxPlayerText = CustomMaxPlayer;
    }
}
