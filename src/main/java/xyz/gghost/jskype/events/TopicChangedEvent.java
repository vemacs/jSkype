package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

@Getter
@AllArgsConstructor
public class TopicChangedEvent extends Event {
    private final Group group;
    private final User user;
    private final String topic;
    private final String oldTopic;
}
