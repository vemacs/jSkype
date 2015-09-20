package xyz.gghost.jskype.internal.impl;

import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.internal.packet.packets.SendMessagePacket;
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
    @Override
    public List<GroupUser> getClients(){
        List<GroupUser> users = new ArrayList<GroupUser>();
        //TODO: add the two people
        return new ArrayList<GroupUser>();
    }
    public String getId() {
        return id.split("8:")[1];
    }
    public String getUsername() {
        return id.split("8:")[1];
    }
    public String getLongId() {
        return id;
    }
    public Message sendMessage(Message msg) {
        return new SendMessagePacket(api).sendMessage(id, msg);
    }
    public Message sendMessage(String msg) {
        return new SendMessagePacket(api).sendMessage(id, new Message(msg));
    }
    public String getTopic() {
        return getUsername();
    }
    public boolean isAdmin() {
        return false;
    }

    public boolean isAdmin(String usr) {
        return false;
    }
}
