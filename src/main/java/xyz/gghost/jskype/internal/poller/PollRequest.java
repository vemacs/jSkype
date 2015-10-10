package xyz.gghost.jskype.internal.poller;

import org.json.JSONObject;
import xyz.gghost.jskype.Group;

public interface PollRequest {
    boolean isMe(JSONObject name);
    void process(JSONObject obj, Group chat);
}
