package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.internal.user.User;


@Getter
public class UserJoinEvent extends Event {
    private final User user;
    private final Group group;

    public UserJoinEvent(Group group, User user) {
        this.user = user;
        this.group = group;
    }
}
