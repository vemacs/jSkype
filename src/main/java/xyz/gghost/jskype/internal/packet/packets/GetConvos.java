package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import org.json.JSONArray;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class GetConvos {
    private SkypeAPI api;

    public GetConvos(SkypeAPI api) {
        this.api = api;
    }


}
