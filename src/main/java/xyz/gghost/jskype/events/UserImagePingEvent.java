package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

@Getter
public class UserImagePingEvent extends Event {
    private final User user;
    private final String imageUrl;
    private final Group chat;

    public UserImagePingEvent(Group group, User user, String imageUrl) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.chat = group;
    }
}
