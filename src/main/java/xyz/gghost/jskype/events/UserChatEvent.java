package xyz.gghost.jskype.events;

import xyz.gghost.jskype.Group;
import lombok.Getter;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.User;

@Getter
public class UserChatEvent extends Event {
    private final User user;
    private final Message msg;
    private final Group chat;
    public UserChatEvent(Group group, User user, Message msg) {
        this.user = user;
        this.msg = msg;
        chat = group;
    }

    public boolean isEdited(){
        return msg.isEdited();
    }
}
