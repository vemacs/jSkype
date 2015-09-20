package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

@Getter
public class UserTypingEvent extends Event {
    private final User user;
    private final Group chat;

    public UserTypingEvent(Group group, User user) {
        this.user = user;
        this.chat = group;
    }
}
