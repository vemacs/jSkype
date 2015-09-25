package xyz.gghost.jskype.internal.packet.packets;

import xyz.gghost.jskype.Chat;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class UserManagementPacket {

    private SkypeAPI api;

    public UserManagementPacket(SkypeAPI api) {
        this.api = api;
    }

    /**
     * @return true = done / false = no perm
     */
    public boolean kickUser(String groupId, String username) {
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
        packet.setType(RequestType.DELETE);
        return packet.makeRequest() != null;
    }

    /**
     * @return true = done / false = no perm
     */
    public boolean addUser(String groupId, String username) {
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
        packet.setData("{\"role\":\"User\"}");
        packet.setType(RequestType.PUT);
        return packet.makeRequest() != null;
    }
}
