package xyz.gghost.jskype.internal.impl;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.RequestBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.user.GroupUser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ghost on 19/09/2015.
 */
public class GroupImpl implements Group {
    @Setter @Getter private String topic = "";
    @Setter @Getter private String id = "";
    @Setter @Getter private String pictureUrl = "";
    private SkypeAPI api;
    @Getter private List<GroupUser> clients = new ArrayList<GroupUser>();

    public GroupImpl(SkypeAPI api, String longId){
        this.id = longId;
        this.api = api;
    }

    public MessageHistory getMessageHistory(){
        if(api.getSkypeInternals().getA().containsKey(id))
            return api.getSkypeInternals().getA().get(id);
        MessageHistory history = new MessageHistory(id, api);
        api.getSkypeInternals().getA().put(id, history);
        return history;
    }

    public void kick(String usr) {
        api.getSkypeInternals().getRequests().getUserRankingRequest().kickUser(getLongId(), usr);
    }

    public void add(String usr) {
        api.getSkypeInternals().getRequests().getUserRankingRequest().addUser(getLongId(), usr);
    }

    public void setAdmin(String usr, boolean admin)
    {
        if (admin) {
            api.getSkypeInternals().getRequests().getUserRankingRequest().promoteUser(getLongId(), usr);
        }else {
            add(usr);
        }
    }

    public String getId() {
        try {
            return NamingUtils.getThreadId(id);
        }catch(Exception e){
            return id;
        }
    }

    public String getLongId() {
        return id;
    }

    public GroupUser getUserByUsername(String username){
        for (GroupUser user : getClients())
            if (user.toString().equals(username))
                return user;
        return null;
    }

    public Message sendMessage(Message msg) {
        return api.getSkypeInternals().getRequests().getSendMessageRequest().sendMessage(id, msg);
    }
    public Message sendMessage(String msg) {
        return sendMessage(new Message(msg));
    }
    public Message sendImage(File url) {
        return api.getSkypeInternals().getRequests().getSendMessageRequest().sendPing(id, new Message(""), api.getSkypeInternals().getRequests().getPingPrepRequest().urlToId(url, id));
    }
    public Message sendImage(URL url) {
        return api.getSkypeInternals().getRequests().getSendMessageRequest().sendPing(id, new Message(""), api.getSkypeInternals().getRequests().getPingPrepRequest().urlToId(url.toString(), id));
    }
    public List<GroupUser> getClients() {
        return clients;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isUserChat(){
        return !getLongId().contains("19:");
    }

    public void leave(){
        kick(api.getUsername());
    }

    public boolean isAdmin() {
        for (GroupUser user : getClients())
            if (user.getUser().getUsername().equals(api.getUsername()) && user.role.equals(GroupUser.Role.MASTER))
                return true;
        return false;
    }

    public boolean isAdmin(String usr) {
        for (GroupUser user : getClients())
            if (user.getUser().getUsername().equals(usr) && user.role.equals(GroupUser.Role.MASTER))
                return true;
        return false;
    }

    public void changeTopic(String topic){
        RequestBuilder pb = new RequestBuilder(api);
        pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + id + "/properties?name=topic");
        pb.setType(RequestType.PUT);
        pb.setData("{\"topic\":\""+topic+"\"}");
        pb.makeRequest();
    }
}
