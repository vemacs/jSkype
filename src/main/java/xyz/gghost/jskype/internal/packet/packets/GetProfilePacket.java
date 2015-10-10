package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.LocalAccountImpl;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.LocalAccount;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.List;

public class GetProfilePacket {
    private SkypeAPI api;
    public GetProfilePacket(SkypeAPI api) {
        this.api = api;
    }


}
