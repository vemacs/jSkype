package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.chat.Chat;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.impl.Group;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.var.*;

import java.util.ArrayList;

public class GetConvos {
    private SkypeAPI api;
    private Skype usr;

    public GetConvos(SkypeAPI api, Skype usr) {
        this.api = api;
        this.usr = usr;
    }

    public ArrayList<Conversation> getRecentChats() throws AccountUnusableForRecentException {
        try {
            ArrayList<Conversation> groups = new ArrayList<Conversation>();

            PacketBuilder options = new PacketBuilder(api);
            options.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=0&pageSize=200&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            options.setData("");
            options.setType(RequestType.OPTIONS);
            options.makeRequest(usr);

            PacketBuilder packet = new PacketBuilder(api);
            packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=0&pageSize=200&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            packet.setData("");
            packet.setType(RequestType.GET);

            String data = packet.makeRequest(usr);

            if (data == null || data.equals(""))
                throw new AccountUnusableForRecentException();

            JSONArray jsonArray = new JSONObject(data).getJSONArray("conversations");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject recent = jsonArray.getJSONObject(i);

                if (recent.getString("targetLink").contains("/contacts/8:")) {
                    groups.add(new Conversation(api, recent.getString("id").split("8:")[1], false));
                } else {
                    String id = recent.getString("id");
                    if (id.endsWith("@thread.skype"))
                        groups.add(new GroupInfoPacket(api, usr).getConvo(id));
                }
            }
            return groups;
        } catch (AccountUnusableForRecentException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
