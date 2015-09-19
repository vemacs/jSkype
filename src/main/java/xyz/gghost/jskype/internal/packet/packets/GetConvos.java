package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import org.json.JSONArray;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.impl.NonContactGroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;


import java.util.List;

public class GetConvos {
    private SkypeAPI api;

    public GetConvos(SkypeAPI api) {
        this.api = api;
    }

    public void setupRecent() throws AccountUnusableForRecentException {
        try {

            PacketBuilder packet = new PacketBuilder(api);
            packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=0&pageSize=200&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            packet.setData("");
            packet.setType(RequestType.GET);

            String data = packet.makeRequest();

            if (data == null || data.equals(""))
                throw new AccountUnusableForRecentException();

            JSONArray jsonArray = new JSONObject(data).getJSONArray("conversations");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject recent = jsonArray.getJSONObject(i);

                if (recent.getString("targetLink").contains("/contacts/8:")) {
                    api.updateGroup(new NonContactGroupImpl(api, recent.getString("id")));
                } else {
                    String id = recent.getString("id");
                    //Old skype for web bug
                    if (id.endsWith("@thread.skype")) {
                        try {
                            api.updateGroup(new GroupInfoPacket(api).getGroup(id));
                        } catch (Exception e) {
                            api.log("WArn: Failed to get convo " + id + " due to rate limiting!");
                        }
                    }
                }
            }
        } catch (AccountUnusableForRecentException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
