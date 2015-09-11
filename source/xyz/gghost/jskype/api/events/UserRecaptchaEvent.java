package xyz.gghost.jskype.api.events;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Ghost on 11/09/2015.
 */
public class UserRecaptchaEvent {
    @Getter @Setter
    private String answer;
    @Getter
    private final String image;
    @Getter
    private final String username;

    public UserRecaptchaEvent(String username, String url) {
        this.username = username;
        this.image = url;
    }

}
