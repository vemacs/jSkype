package xyz.gghost.jskype.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.event.Event;

@EqualsAndHashCode(callSuper = false)

public class UserRecaptchaEvent extends Event {
    @Getter private final String image;
    @Getter private final String username;
    @Setter @Getter private String answer;
    public UserRecaptchaEvent(String img, String user){
        image = img;
        username = user;
    }
}
