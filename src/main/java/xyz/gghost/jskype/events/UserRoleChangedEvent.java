package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

@Getter
@AllArgsConstructor
public class UserRoleChangedEvent extends Event {
    private final Group chat;
    private final User user;
    private final GroupUser.Role role;
    private final GroupUser updater;

    public boolean isPromoted(){
        return role == GroupUser.Role.MASTER;
    }
    public boolean isDemoted(){
        return role != GroupUser.Role.MASTER;
    }

}
