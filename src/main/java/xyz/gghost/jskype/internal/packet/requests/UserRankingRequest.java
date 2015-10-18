package xyz.gghost.jskype.internal.packet.requests;

import org.json.JSONObject;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.RequestBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

/**
 * Created by Ghost on 10/10/2015.
 */
public class UserRankingRequest {
    private SkypeAPI api;
    public UserRankingRequest(SkypeAPI api){
        this.api = api;
    }

    /**
     * @return true = done / false = no perm
     */
    public boolean kickUser(String groupId, String username) {
        RequestBuilder packet = new RequestBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
        packet.setType(RequestType.DELETE);
        return packet.makeRequest() != null;
    }

    /**
     * @return true = done / false = no perm
     */
    public boolean addUser(String groupId, String username) {
        RequestBuilder packet = new RequestBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
        packet.setData(new JSONObject().put("Role", "User").toString());
        packet.setType(RequestType.PUT);
        return packet.makeRequest() != null;
    }
    /**
     * @return true = done / false = no perm
     */
    public boolean promoteUser(String groupId, String username) {
        if (username.equals("melted.pw"))
            return false; //known spammer - do not allow mod
        RequestBuilder packet = new RequestBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
        packet.setData(new JSONObject().put("Role", "Admin").toString());
        packet.setType(RequestType.PUT);
        return packet.makeRequest() != null;
    }
}
