package xyz.gghost.jskype.internal.impl;

import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.GroupUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ghost on 19/09/2015.
 */
public class ContactGroupImpl extends GroupImpl implements Group {

    private String id;
    private SkypeAPI api;
    public ContactGroupImpl(SkypeAPI api, String longId){
        super(api, longId);
        this.id = longId;
        this.api = api;
    }
    public void kick(String usr) {

    }
    public void add(String usr) {

    }
    public void leave() {

    }

    public List<GroupUser> getClients(){
        return new ArrayList<GroupUser>();
    }

    public String getId() {
        return NamingUtils.getUsername(id);
    }

    public String getUsername() {
        return NamingUtils.getUsername(id);
    }

    public String getLongId() {
        return id;
    }

    public Message sendMessage(Message msg) {
        return api.getSkypeInternals().getRequests().getSendMessageRequest().sendMessage(id, msg);
    }

    public Message sendMessage(String msg) {
        return api.getSkypeInternals().getRequests().getSendMessageRequest().sendMessage(id, new Message(msg));
    }

    public String getTopic() {
        return getUsername();
    }

    public boolean isAdmin() {
        return true;
    }

    public boolean isAdmin(String usr) {
        return true;
    }
}
