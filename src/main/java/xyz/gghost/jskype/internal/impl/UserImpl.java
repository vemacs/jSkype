package xyz.gghost.jskype.internal.impl;

import lombok.Data;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.user.OnlineStatus;
import xyz.gghost.jskype.user.User;

@Data
public class UserImpl implements User {
    private String displayName;
    private String username;
    private String pictureUrl = "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png";
    private String mood = "";
    private boolean isContact = false;
    private boolean blocked = false;
    private String firstName = "";
    private String lastName = "";
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

    public UserImpl() {}

    public UserImpl(String username) {
        displayName = username;
        this.username = username;
    }

    public MessageHistory getMessageHistory(SkypeAPI api){
        if(api.getSkypeInternals().getA().containsKey(username))
            return api.getSkypeInternals().getA().get(username);
        MessageHistory history = new MessageHistory(username, api);
        api.getSkypeInternals().getA().put(username, history);
        return history;
    }

    public String toString(){
        return username;
    }

    public void sendContactRequest(SkypeAPI api){
        api.sendContactRequest(username);
    }

    public void sendContactRequest(SkypeAPI api, String hello){
        api.sendContactRequest(username, hello);
    }

    public Group getGroup(SkypeAPI api){
        return new ContactGroupImpl(api, "8:" + username);
    }
}
