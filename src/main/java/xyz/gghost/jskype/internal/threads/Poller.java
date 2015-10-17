package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.poller.*;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.user.GroupUser;

import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.List;

public class Poller extends Thread {

    private SkypeAPI api;
    public List<PollRequest> processors = new ArrayList<PollRequest>();
    public ArrayList<Integer> pastIds = new ArrayList<Integer>();
    public Poller(SkypeAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        processors.addAll(new ArrayList<PollRequest>() {{
            add(new AdsPoll(api));
            add(new MessagePoll(api));
            add(new OnlineStatusPoll(api));
            add(new PicturePoll(api));
            add(new ThreadUpdatePoll(api));
            add(new TopicUpdatePoll(api));
            add(new PingPoll(api));
        }});

        while(true) {
            poll(this);
        }

    }

    public void stopThreads(){

    }

    private void poll(Thread h) {
        try {
            PacketBuilder poll = new PacketBuilder(api);
            poll.setType(RequestType.POST);
            poll.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll");
            poll.setData(" ");

            String data = poll.makeRequest();

            if (data == null || data.equals("") || data.equals("{}"))
                return;

            JSONArray json = new JSONObject(data).getJSONArray("eventMessages");
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = json.getJSONObject(i);

                if (!(object.isNull("type") && object.isNull("resourceType"))) {

                    String resourceLink = object.isNull("resourceLink") ? "" : object.getString("resourceLink");
                    JSONObject resource = object.getJSONObject("resource");

                    if (resource.isNull("resourceLink"))
                        resource.put("resourceLink", resourceLink);

                    if (api.isDebugMode())
                        System.out.println(object);

                    Group chat = null;

                    if (resourceLink.contains("conversations/19:") || resourceLink.contains("8:")) {
                        if (!resourceLink.contains("8:")) {
                            addGroupToRecent(object);
                            chat = api.getGroupById(NamingUtils.getThreadId(resourceLink));
                        } else if (!resourceLink.contains("@")) {
                            chat = new ContactGroupImpl(api, "8:" + NamingUtils.getUsername(resourceLink));
                        }
                    }


                    if ((!object.isNull("id")) && (pastIds.contains(object.getInt("id")))) {
                        if (api.isDebugMode())
                            api.log("Warning! Skype might be behind us!");
                        continue;
                    }

                    for (PollRequest pollProcessor : processors) {
                        try {
                            if (pollProcessor.isMe(object))
                                pollProcessor.process(resource, chat);
                        } catch (Exception e) {
                            api.log("Failed to process data from skype.\nMessagePoll: " + object + "Data: " + data + "\nError: " + e.getMessage());
                            api.log("\n\nIs this a new convo?\nWait a few seconds!");
                            e.printStackTrace();
                        }
                    }

                    if (!object.isNull("id"))
                        pastIds.add(object.getInt("id"));
                }
            }
        }catch(Exception e){}
    }


    public static User getUser(String username, Group chat, SkypeAPI api) {
        User user = api.getContact(username);
        if (user == null)
            for (GroupUser users : chat.getClients())
                if (users.getUser().getUsername().equals(username))
        if (user == null)
            user = api.getSkypeInternals().getRequests().getUserMetaRequest().getUser(username);
        return user;
    }

    private void addGroupToRecent(JSONObject object) {
        String resourceL = object.getString("resourceLink");
        if (resourceL.contains("endpoint"))
            return;
        try {
            String idLong = resourceL.split("conversations/")[1].split("/")[0];

            for (Group group : api.getGroups())
                if (group.getLongId().equals(idLong))
                    return;

            api.updateGroup(api.getSkypeInternals().getRequests().getGroupMetaRequest().getGroup(idLong));
        } catch (Exception e){}
    }

}
