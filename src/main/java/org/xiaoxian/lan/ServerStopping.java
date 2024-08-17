package org.xiaoxian.lan;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.xiaoxian.EasyLAN;

public class ServerStopping {

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event)
    {
        if (event.getServer().isSinglePlayer() && EasyLAN.HttpAPI) {
            new ShareToLan().handleStop();
        }
    }
}
