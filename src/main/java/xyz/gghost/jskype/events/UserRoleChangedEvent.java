package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

@Getter
public class UserRoleChangedEvent extends Event {
    private final User user;
    private final GroupUser.Role role;
    private final Group chat;
    private final GroupUser updater;
    public UserRoleChangedEvent(Group group, User user, GroupUser.Role role, GroupUser updater) {
        this.user = user;
        this.role = role;
        this.updater = updater;
        chat = group;
    }

    public boolean isPromoted(){
        return role == GroupUser.Role.MASTER;
    }
    public boolean isDemoted(){
        return role != GroupUser.Role.MASTER;
    }

}
