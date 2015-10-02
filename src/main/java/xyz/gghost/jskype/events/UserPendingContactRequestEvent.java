package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.event.Event;

@Getter
public class UserPendingContactRequestEvent extends Event {
    private String user;
    public UserPendingContactRequestEvent(String user){
        this.user = user;
    }

    public void accept(SkypeAPI api){
        api.acceptContactRequest(user);
    }
}
