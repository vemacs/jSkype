package xyz.gghost.jskype.events;

import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

@Getter
public class TopicChangedEvent extends Event {
    private final String topic;
    private final Group group;
    private final User user;
    private final String oldTopic;

    public TopicChangedEvent(Group group, User user, String topic, String oldTopic) {
        this.topic = topic;
        this.group = group;
        this.oldTopic = oldTopic;
        this.user = user;
    }
}
