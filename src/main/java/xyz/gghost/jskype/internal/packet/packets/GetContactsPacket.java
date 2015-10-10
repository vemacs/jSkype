package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import org.json.JSONArray;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.FailedToGetContactsException;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GetContactsPacket {

    private final SkypeAPI api;

    public GetContactsPacket(SkypeAPI api) {
        this.api = api;
    }


}

