package xyz.gghost.jskype.internal.packet.packets;

import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class PingPacket {
    private SkypeAPI api;

    public PingPacket(SkypeAPI api) {
        this.api = api;

    }

    public void doNow() {
        PacketBuilder ping = new PacketBuilder(api);
        ping.setType(RequestType.POST);
        ping.setUrl("https://web.skype.com/api/v1/session-ping");
        ping.setData("sessionId=" + api.getUuid().toString());
        ping.setIsForm(true);
        String data = ping.makeRequest(api.getSkype());
        if (data == null || data.equals("---")) {
            if (api.displayInfoMessages())
                System.out.println("Skype login expired... Reconnecting");
            try {
                api.getSkype().relog();
                api.getPoller().prepare();
            }catch(Exception e){
                if (api.displayInfoMessages())
                  System.out.println("Failed to reconnect. ?");
            }
        }

    }

}
