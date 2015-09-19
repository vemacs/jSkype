package xyz.gghost.jskype.internal.user;

import lombok.Data;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.NonContactGroupImpl;

@Data
public class User {
    private String displayName;
    private String username;
    private String pictureUrl = "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png";
    private String mood = "";
    private boolean isContact = false;

    public User() {
    }

    public User(String username) {
        displayName = username;
        this.username = username;
    }
    public void sendContactRequest(SkypeAPI api){
        api.sendContactRequest(username);
    }
    public void sendContactRequest(SkypeAPI api, String hello){
        api.sendContactRequest(username, hello);
    }
    public Group getGroup(SkypeAPI api){
        return new NonContactGroupImpl(api, "8:" + username);
    }
}
