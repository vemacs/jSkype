package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.internal.user.User;

@Deprecated
/**
 * Event not in use - careful when using UserTypingEvent
 */
@Getter
public class UserStoppedTypingEvent extends Event {

    private final User user;
    private final Group group;

    public UserStoppedTypingEvent(Group group, User user) {
        this.user = user;
        this.group = group;
    }
}
