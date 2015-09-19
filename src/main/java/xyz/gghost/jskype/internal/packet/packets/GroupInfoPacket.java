package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import xyz.gghost.jskype.Chat;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.user.GroupUser;
import xyz.gghost.jskype.internal.user.User;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoPacket {
    private SkypeAPI api;

    public GroupInfoPacket(SkypeAPI api){
        this.api = api;
    }


    public Group getGroup(String longId){
        List<GroupUser> groupMembers = new ArrayList<GroupUser>();

        PacketBuilder members = new PacketBuilder(api);
        members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + longId + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
        members.setType(RequestType.GET);

        String data = members.makeRequest();

        if (data == null)
            return null;

        GroupImpl group = new GroupImpl(api, new JSONObject(data).getString("id"));

        JSONObject properties = new JSONObject(data).getJSONObject("properties");
        if (!properties.isNull("topic"))
            group.setTopic(properties.getString("topic"));

        if (!properties.isNull("picture"))
            group.setPictureUrl(properties.getString("picture").split("@")[1]);

        group.setTopic(Chat.decodeText(group.getTopic()));

        JSONArray membersArray = new JSONObject(data).getJSONArray("members");
        for (int ii = 0; ii < membersArray.length(); ii++) {
            JSONObject member = membersArray.getJSONObject(ii);
            try {
                GroupUser.Role role = GroupUser.Role.USER;
                User ussr = api.getSimpleUser(member.getString("id").split(":")[1]);
                if (!member.getString("role").equals("User"))
                    role = GroupUser.Role.MASTER;
                group.getClients().add(new GroupUser(ussr, role));
            } catch (Exception e){
                api.log("Failed to get a member info");
            }
        }
        return group;
    }

    public List<GroupUser> getUsers(String id) {
        try {
            ArrayList<GroupUser> groupMembers = new ArrayList<GroupUser>();

            PacketBuilder members = new PacketBuilder(api);
            members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + id + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            members.setType(RequestType.GET);

            String data = members.makeRequest();

            if (data == null)
                return null;

            JSONArray membersArray = new JSONObject(data).getJSONArray("members");
            for (int ii = 0; ii < membersArray.length(); ii++) {
                JSONObject member = membersArray.getJSONObject(ii);
                try {

                    GroupUser.Role role = GroupUser.Role.USER;
                    User ussr = api.getSimpleUser(member.getString("id").split(":")[1]);
                    if (!member.getString("role").equals("User"))
                        role = GroupUser.Role.MASTER;

                    GroupUser gu = new GroupUser(ussr, role);
                    groupMembers.add(gu);
                } catch (Exception e){
                    api.log("Failed to get a member info");
                }
            }
            return groupMembers;
        } catch(NullPointerException e) {
            return null;
        } catch(JSONException e) {
            return null;
        }

    }
}
