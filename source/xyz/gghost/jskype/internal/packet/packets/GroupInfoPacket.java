package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.chat.Chat;
import xyz.gghost.jskype.internal.impl.Group;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.var.Conversation;
import xyz.gghost.jskype.var.GroupUser;
import xyz.gghost.jskype.var.Role;
import xyz.gghost.jskype.var.User;

import java.util.ArrayList;

/**
 * Created by Ghost on 06/09/2015.
 */
public class GroupInfoPacket {
    private SkypeAPI api;
    private Skype skype;



    public GroupInfoPacket(SkypeAPI api, Skype skype){
        this.skype = skype;
        this.api = api;
    }
    public Conversation getConvo(String longId){
        try {
            Group group = getGroup(longId);
            Conversation theChat = new Conversation(api, group.getChatId(), true);
            theChat.setForcedGroup(true);
            theChat.setForcedGroupGroup(group);
            return theChat;
        }catch(NullPointerException e){
            Conversation convo = skype.getConvoByShortId(longId.split("19:")[1].split("@")[0]);
            if (convo == null)
                throw e;
            return convo;
        }
    }

    public Group getGroup(String longId){
        ArrayList<GroupUser> groupMembers = new ArrayList<GroupUser>();

        PacketBuilder members = new PacketBuilder(api);
        members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + longId + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
        members.setType(RequestType.GET);

        String data = members.makeRequest(skype);

        if (data == null) {
            return null;
        }

        String id = new JSONObject(data).getString("id").split(":")[1].split("@")[0];

        Group group = new Group(id, "", null);
        group.setChatId(id);
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

                Role role = Role.USER;
                User ussr = skype.getSimpleUser(member.getString("id").split(":")[1]);
                if (!member.getString("role").equals("User"))
                    role = Role.ADMIN;

                GroupUser gu = new GroupUser(ussr);
                gu.setRole(role);

                groupMembers.add(gu);
            } catch (Exception e){
                if(api.isDebugMode())
                    System.out.println("Failed to get a member info");
            }
        }
        group.setConnectedClients(groupMembers);
        return group;
    }

    public ArrayList<GroupUser> getUsers(String id) {
        try {
            ArrayList<GroupUser> groupMembers = new ArrayList<GroupUser>();

            PacketBuilder members = new PacketBuilder(api);
            members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + id + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            members.setType(RequestType.GET);

            String data = members.makeRequest(skype);

            if (data == null)
                return null;

            JSONArray membersArray = new JSONObject(data).getJSONArray("members");
            for (int ii = 0; ii < membersArray.length(); ii++) {
                JSONObject member = membersArray.getJSONObject(ii);
                try {

                    Role role = Role.USER;
                    User ussr = skype.getSimpleUser(member.getString("id").split(":")[1]);
                    if (!member.getString("role").equals("User"))
                        role = Role.ADMIN;

                    GroupUser gu = new GroupUser(ussr);
                    gu.setRole(role);

                    groupMembers.add(gu);
                } catch (Exception e){
                    if(api.isDebugMode())
                        System.out.println("Failed to get a member info");
                }
            }
            return groupMembers;
        }catch(NullPointerException | JSONException e){
            return null;
        }

    }
}
