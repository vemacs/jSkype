package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.Chat;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.*;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.GetProfilePacket;
import xyz.gghost.jskype.internal.packet.packets.GroupInfoPacket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang3.StringEscapeUtils;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;

public class Poller extends Thread {

    private SkypeAPI api;

    public Poller(SkypeAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        while (this.isAlive()) {
            poll();
        }
    }

    private void poll() {
        PacketBuilder poll = new PacketBuilder(api);
        poll.setType(RequestType.POST);
        poll.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll");
        poll.setData(" ");
        String data = poll.makeRequest();
        if (data == null || data.equals("") || data.equals("{}"))
            return;


        JSONObject messagesAsJson = new JSONObject(data);
        JSONArray json = messagesAsJson.getJSONArray("eventMessages");
        for (int i = 0; i < json.length(); i++) {
            JSONObject object = json.getJSONObject(i);
            try {
                if (!(object.isNull("type") && object.isNull("resourceType"))) {
                    Group chat = null;

                    if (object.getString("resourceLink").contains("conversations/19:") || object.getString("resourceLink").contains("8:")) {
                        if (!object.getString("resourceLink").contains("8:") ) {
                            String idShort = object.getString("resourceLink").split("conversations/19:")[1].split("@thread")[0];
                            addGroupToRecent(object);
                            chat = api.getGroupById(idShort);
                            //Old skype for web bug that affects some users
                        } else if (!object.getString("resourceLink").contains("@")){
                            chat = new ContactGroupImpl(api, "8:" + object.getString("resourceLink").split("/8:")[1].split("/")[0]);
                        }
                    } else {
                        //api.log("Non-group data received from skype. This is ignorable.");
                    }

                    //thread update
                    if (object.getString("resourceType").equals("ThreadUpdate")) {
                        Group oldGroup = null;

                        ArrayList<String> oldUsers2 = new ArrayList<String>();
                        ArrayList<String> oldUsers = new ArrayList<String>();
                        ArrayList<String> newUsers = new ArrayList<String>();

                        String shortId = object.getString("resourceLink").split("19:")[1].split("@")[0];
                        for (Group groups : api.getGroups()) {
                            if (groups.getId().equals(shortId)) {
                                for (GroupUser usr : groups.getClients()) { //NULL POINTER HERE
                                    oldUsers.add(usr.getUser().getUsername().toLowerCase());
                                    oldUsers2.add(usr.getUser().getUsername().toLowerCase());
                                }
                                oldGroup = groups;
                            }
                        }

                        if (oldGroup != null) {

                            JSONObject resource = object.getJSONObject("resource");

                            String topic = resource.getJSONObject("properties").isNull("properties") ? "" : resource.getJSONObject("properties").getString("properties");
                            String picture = resource.getJSONObject("properties").isNull("picture") ? "" : resource.getJSONObject("properties").getString("picture");
                            GroupImpl group = new GroupImpl(api, topic);
                            group.setPictureUrl(picture);
                            group.setTopic(topic);
                            //user join/leave events
                            for (int ii = 0; ii < object.getJSONObject("resource").getJSONArray("members").length(); ii++) {
                                JSONObject user = object.getJSONObject("resource").getJSONArray("members").getJSONObject(ii);
                                newUsers.add(user.getString("id").replace("8:", ""));
                                try {
                                    GroupUser.Role role = GroupUser.Role.USER;
                                    User ussr = api.getSimpleUser(user.getString("id").replace("8:", ""));
                                    if (!user.getString("role").equals("User"))
                                        role = GroupUser.Role.MASTER;
                                    GroupUser gu = new GroupUser(ussr, role);
                                    group.getClients().add(gu);
                                } catch (Exception ignored) {
                                }
                            }


                            //completely reupdate
                            api.getGroups().remove(oldGroup);
                            api.getGroups().add(group);

                            oldUsers.removeAll(newUsers);
                            newUsers.removeAll(oldUsers2);
                            //get difference
                            for (String old : oldUsers) {
                                if (!old.equals("live"))
                                    api.getEventManager().executeEvent(new UserLeaveEvent(group, new GetProfilePacket(api).getUser(old)));
                            }
                            for (String news : newUsers) {
                                if (!news.equals("live"))
                                    api.getEventManager().executeEvent(new UserJoinEvent(group, new GetProfilePacket(api).getUser(news)));
                            }
                        }
                    }

                    //resource json
                    JSONObject resource = object.getJSONObject("resource");

                    //Get topic update
                    if (!resource.isNull("messagetype") && resource.getString("messagetype").equals("ThreadActivity/TopicUpdate")) {
                        String topic = resource.getString("content").split("<value>")[1].split("<\\/value>")[0];
                        topic = Chat.decodeText(topic);

                        String username = resource.getString("content").split("<initiator>8:")[1].split("<\\/initiator>")[0];
                        String oldTopic = api.getGroupById(chat.getId()).getTopic();

                        User user = getUser(username, chat);
                        api.getEventManager().executeEvent(new TopicChangedEvent(chat, user, topic, oldTopic));

                        ((GroupImpl)api.getGroupById(chat.getId())).setTopic(topic);
                    }

                    //Get Typing
                    if (!resource.isNull("messagetype") && resource.getString("messagetype").equals("Control/Typing")) {
                        User from = getUser(resource.getString("from").split("8:")[1], chat);
                        api.getEventManager().executeEvent(new UserTypingEvent(chat, from));
                    }


                    //Get message
                    if (!resource.isNull("messagetype") && (resource.getString("messagetype").equals("RichText") || resource.getString("messagetype").equals("Text"))) {

                        Message message = new Message();
                        User user = getUser(resource.getString("from").split("8:")[1], chat);

                        String content = "";
                        if(!resource.isNull("content"))
                            content = Chat.decodeText(resource.getString("content"));

                        if (!resource.isNull("clientmessageid"))
                            message.setId(resource.getString("clientmessageid"));

                        if (!resource.isNull("skypeeditedid")) {
                            content = content.replaceFirst("Edited previous message: ", "").split("<e_m")[0];
                            message.setId(resource.getString("skypeeditedid"));
                            message.setEdited(true);
                        }

                        message.setSender(user);
                        message.setTime(resource.getString("originalarrivaltime"));
                        message.setUpdateUrl(object.getString("resourceLink").split("/messages/")[0] + "/messages");
                        message.setMessage(content);

                        api.getEventManager().executeEvent(new UserChatEvent(chat, user, message));

                    }

                    //pings
                    if (!resource.isNull("messagetype") && resource.getString("messagetype").startsWith("RichText/")) {
                        User user = getUser(resource.getString("from").split("8:")[1], chat);
                        String content = resource.getString("content");
                        content = StringEscapeUtils.unescapeHtml4(content);
                        content = StringEscapeUtils.unescapeHtml3(content);
                        if (content.contains("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?")) {

                            String id = content.split("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?")[1].split("\">")[0];
                            String url = ("https://api.asm.skype.com/v1/objects/" + id + "/views/imgo").replace("objects/?", "objects/");
                            api.getEventManager().executeEvent(new UserImagePingEvent(chat, user, url));
                            return;
                        }
                        if (content.contains("<files alt=\"") && content.contains("<file size=")) {
                            api.getEventManager().executeEvent(new UserOtherFilesPingEvent(chat, user));
                            return;
                        }
                    }
                }

            }catch (Exception e) {
                System.out.println("Failed to process data from skype.\nMessage: "  + object + "Data: " + data + "\nError: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //OLD METHODS
    private User getUser(String username, Group chat) {
        User user;
        //get user from contacts
        user = api.getContact(username);
        //get user from connected clients
        if (user == null) {
            try {
                for (GroupUser users : chat.getClients()) {
                    if (users.getUser().getUsername().equals(username))
                        user = users.getUser();
                }
            } catch (NullPointerException ignored) {}
        }
        //If failed to get user - get the users info by calling skypes api
        if (user == null)
            user = new GetProfilePacket(api).getUser(username);
        return user;
    }

    private void addGroupToRecent(JSONObject object) {
        if (object.getString("resourceLink").contains("endpoint"))
            return;
        try {
            String idLong = object.getString("resourceLink").split("conversations/")[1].split("/")[0];
            String idShort = object.getString("resourceLink").split("conversations/19:")[1].split("@thread")[0];
            //get if already exists
            for (Group group : api.getGroups()) {
                if (group.getId().equals(idShort))
                    return;
            }
            api.getGroups().add(new GroupInfoPacket(api).getGroup(idLong));
        }catch(Exception e){
            System.out.println(object.getString("resourceLink"));
            e.printStackTrace();
        }
    }

}
