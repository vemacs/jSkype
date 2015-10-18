package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.event.Event;

@Getter
@AllArgsConstructor
public class UserPendingContactRequestEvent extends Event {
    private final String user;

    public void accept(SkypeAPI api){
        api.acceptContactRequest(user);
    }
}
