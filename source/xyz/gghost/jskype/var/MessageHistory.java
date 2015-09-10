package xyz.gghost.jskype.var;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.chat.Chat;
import xyz.gghost.jskype.internal.impl.Group;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.var.Conversation;
import xyz.gghost.jskype.var.GroupUser;
import xyz.gghost.jskype.var.Message;
import xyz.gghost.jskype.var.User;

import java.util.ArrayList;

public class MessageHistory {
    private String longId;
    private SkypeAPI api;
    private Skype skype;
    private String nextUrl = null;
    @Getter
    private ArrayList<Message> knownMessages = new ArrayList<Message>();
    public MessageHistory(String longId, SkypeAPI api, Skype skype){
        this.longId = longId;
        this.api = api;
        this.skype = skype;
        loadMoreMessages();
    }
    public void loadMoreMessages(){

        Conversation convo = new Conversation(api, longId, true);

        if(longId.contains("8:")){
            convo.setId(longId.split("8:")[1]);
            convo.setUserChat(true);
        }else{
            Group group = skype.getGroupById(longId.split(":")[1].split("@")[0]);
            convo.setId(group.getChatId());
            convo.setForcedGroup(true);
            convo.setForcedGroupGroup(group);
            convo.setUserChat(false);
        }


        String nextUrl = this.nextUrl;
        if (nextUrl == null)
            nextUrl = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages?startTime=0&pageSize=51&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread";
        PacketBuilder builder = new PacketBuilder(api);
        builder.setType(RequestType.GET);
        builder.setUrl(nextUrl);

        String data = builder.makeRequest(skype);
        if (data == null)
            return;

        JSONObject json = new JSONObject(data);

        if (!json.getJSONObject("_metadata").isNull("syncState"))
            this.nextUrl = json.getJSONObject("_metadata").getString("syncState");


        JSONArray jsonArray = json.getJSONArray("messages");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonMessage = jsonArray.getJSONObject(i);
            if(jsonMessage.getString("type").equals("Message")) {
                Message message = new Message(Chat.decodeText(jsonMessage.getString("content")));
                User user = null;
                try {
                     user = getUser(jsonMessage.getString("from").split("8:")[1], convo);
                }catch (Exception e ){
                    continue;
                }
                String content = "";
                if(!jsonMessage.isNull("content"))
                    content = Chat.decodeText(jsonMessage.getString("content"));
                if (!jsonMessage.isNull("clientmessageid"))
                    message.setId(jsonMessage.getString("clientmessageid"));
                if (!jsonMessage.isNull("skypeeditedid")) {
                    content = Chat.decodeText(content.replaceFirst("Edited previous message: ", "").split("<e_m")[0]);
                    message.setId(jsonMessage.getString("skypeeditedid"));
                    message.setEdited(true);
                }
                message.setSender(user);
                message.setTime(jsonMessage.getString("originalarrivaltime"));
                message.setUpdateUrl("https://db3-client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages");
                message.setMessage(content);
                knownMessages.add(message);
            }
        }
    }
    public int knownMessagesCount(){
        return knownMessages.size();
    }
    private User getUser(String username, Conversation chat) {
        User user = null;
        //get user from contacts
        user = skype.getContact(username);
        //get user from connected clients
        if (user == null) {
            try {
                for (GroupUser users : chat.getConnectedClients()) {
                    if (users.getAccount().getUsername().equals(username))
                        user = users.getAccount();
                }
            } catch (NullPointerException e) {}
        }
        //If failed to get user - get the users info by calling skypes api
        if (user == null)
            user = skype.getSimpleUser(username);
        return user;
    }
}
