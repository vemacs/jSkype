package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.Group;
import lombok.Getter;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.User;

@Getter
@AllArgsConstructor
public class UserChatEvent extends Event {
    private final Group chat;
    private final User user;
    private final Message msg;

    public boolean isEdited(){
        return msg.isEdited();
    }
}
