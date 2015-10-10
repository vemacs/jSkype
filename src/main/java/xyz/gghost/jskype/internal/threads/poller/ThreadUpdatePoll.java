package xyz.gghost.jskype.internal.threads.poller;

import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserJoinEvent;
import xyz.gghost.jskype.events.UserLeaveEvent;
import xyz.gghost.jskype.events.UserRoleChangedEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;

public class ThreadUpdatePoll implements PollRequest {

    /*

            THIS CLASS IS HACKKY || RECODE IS A TODO

     */
    private SkypeAPI api;
    public ThreadUpdatePoll(SkypeAPI api){
        this.api = api;
    }
    public void process(JSONObject resource, Group chat) {
        try{
            Group oldGroup = null;

            ArrayList<String> oldUsers2 = new ArrayList<String>();
            ArrayList<String> oldUsers = new ArrayList<String>();
            ArrayList<String> newUsers = new ArrayList<String>();


            String shortId = NamingUtils.getThreadId(resource.getString("resourceLink"));

            for (Group groups : api.getGroups()) {
                if (groups.getId().equals(shortId)) {
                    for (GroupUser usr : groups.getClients()) {
                        oldUsers.add(usr.getUser().getUsername().toLowerCase());
                        oldUsers2.add(usr.getUser().getUsername().toLowerCase());
                    }
                    oldGroup = groups;
                }
            }

            if (oldGroup != null) {

                String topic = resource.getJSONObject("properties").isNull("properties") ? "" : resource.getJSONObject("properties").getString("properties");

                if ((api.getGroupById(shortId) != null))
                    topic = api.getGroupById(shortId).getTopic();

                String picture = resource.getJSONObject("properties").isNull("picture") ? "" : resource.getJSONObject("properties").getString("picture");

                if ((api.getGroupById(shortId) != null) && (!api.getGroupById(shortId).getPictureUrl().equals(picture)))
                    picture = api.getGroupById(shortId).getPictureUrl();

                GroupImpl group = new GroupImpl(api, "19:" + shortId + "@thread.skype");

                group.setPictureUrl(picture);
                group.setTopic(topic);


                for (int ii = 0; ii < resource.getJSONArray("members").length(); ii++) {
                    JSONObject user = resource.getJSONArray("members").getJSONObject(ii);

                    if (!(user.getString("id").contains("8:")))
                        continue;

                    newUsers.add(user.getString("id").split("8:")[1]); //add username

                    try {
                        GroupUser.Role role = GroupUser.Role.USER;

                        User usr = api.getSimpleUser(user.getString("id").split("8:")[1]); //get user without searching skypes BD

                        if (!user.getString("role").equals("User"))
                            role = GroupUser.Role.MASTER;

                        if (oldUsers.contains(usr.getUsername()))
                            for (GroupUser users : oldGroup.getClients())
                                if (users.getUser().getUsername().equals(usr.getUsername()))
                                    if (role != users.role)
                                        api.getEventManager().executeEvent(new UserRoleChangedEvent(oldGroup, users.getUser(), role));

                        GroupUser gu = new GroupUser(usr, role, group);
                        group.getClients().add(gu);

                    } catch (Exception ignored) {

                    }
                }


                oldUsers.removeAll(newUsers);
                newUsers.removeAll(oldUsers2);


                for (String old : oldUsers)
                    api.getEventManager().executeEvent(new UserLeaveEvent(group,  api.getSkypeInternals().getRequests().getUserMetaRequest().getUser(old)));

                for (String news : newUsers)
                    api.getEventManager().executeEvent(new UserJoinEvent(group, api.getSkypeInternals().getRequests().getUserMetaRequest().getUser(news)));

                api.updateGroup(group);
            }
        }catch(Exception e){
            api.log("#################################################");
            if (api.isAllowLogging())
                e.printStackTrace();
            api.log("#################################################");
            api.log("Failed to update group info. Have we just loaded?");
            try{
                api.log("Resource Link: " + resource.getString("resourceLink"));
                api.log("Group ID: " + resource.getString("resourceLink").split("19:")[1].split("@")[0] + "@thread.skype");
            }catch(Exception ea){}
        }

    }

    public boolean isMe(JSONObject name){
        return ((!name.isNull("resourceType")) && name.getString("resourceType").equals("ThreadUpdate"));
    }


}
