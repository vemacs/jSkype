package xyz.gghost.jskype.internal.packet.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.packet.RequestBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ghost on 10/10/2015.
 */
public class GroupMetaRequest {
    private SkypeAPI api;
    public GroupMetaRequest(SkypeAPI api){
        this.api = api;
    }
    public Group getGroup(String longId){
        List<GroupUser> groupMembers = new ArrayList<GroupUser>();

        RequestBuilder members = new RequestBuilder(api);
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


        JSONArray membersArray = new JSONObject(data).getJSONArray("members");
        for (int ii = 0; ii < membersArray.length(); ii++) {
            JSONObject member = membersArray.getJSONObject(ii);
            String id = member.getString("id");

            try {
                GroupUser.Role role = GroupUser.Role.USER;

                if (!id.contains("8:"))
                    continue;

                User usr = api.getSimpleUser(NamingUtils.getUsername(id));

                if (!member.getString("role").equals("User"))
                    role = GroupUser.Role.MASTER;

                group.getClients().add(new GroupUser(usr, role, group));
            } catch (Exception e){
                api.log("Failed to get a members info");
                if (api.isDebugMode())
                    e.printStackTrace();
            }
        }

        String topic = group.getTopic();
        if (topic.equals("")){
            for (GroupUser user : group.getClients())
                topic = topic + ", " + user.getUser().getUsername();
            topic = topic.replaceFirst(", ", "");
        }

        group.setTopic(FormatUtils.decodeText(topic));
        return group;
    }

    public List<GroupUser> getUsers(String id) {
        try {
            ArrayList<GroupUser> groupMembers = new ArrayList<GroupUser>();

            RequestBuilder members = new RequestBuilder(api);
            members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + id + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            members.setType(RequestType.GET);

            String data = members.makeRequest();

            if (data == null)
                return null;

            JSONArray membersArray = new JSONObject(data).getJSONArray("members");

            for (int ii = 0; ii < membersArray.length(); ii++) {
                JSONObject member = membersArray.getJSONObject(ii);
                String ia = member.getString("id");
                try {

                    GroupUser.Role role = GroupUser.Role.USER;

                    if (!ia.contains("8:"))
                        continue;

                    User usr = api.getSimpleUser(NamingUtils.getUsername(ia));

                    if (!member.getString("role").equals("User"))
                        role = GroupUser.Role.MASTER;

                    GroupUser gu = new GroupUser(usr, role, new GroupImpl(api, id));

                    groupMembers.add(gu);

                } catch (Exception e){
                    api.log("Failed to get a members info");
                    if (api.isDebugMode())
                        e.printStackTrace();
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
