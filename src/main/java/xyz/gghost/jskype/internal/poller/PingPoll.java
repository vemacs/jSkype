package xyz.gghost.jskype.internal.poller;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserImagePingEvent;
import xyz.gghost.jskype.events.UserOtherFilesPingEvent;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class PingPoll implements PollRequest {
    private SkypeAPI api;

    public void process(JSONObject obj, Group chat) {
        User user = Poller.getUser(obj.getString("from").split("8:")[1], chat, api);
        String content = FormatUtils.decodeText(obj.getString("content"));

        if (content.contains("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?")) {
            String id = content.split("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?")[1].split("\">")[0];
            String url = ("https://api.asm.skype.com/v1/objects/" + id + "/views/imgo").replace("objects/?", "objects/");
            api.getEventManager().executeEvent(new UserImagePingEvent(chat, user, url));
            return;
        }

        if (content.contains("<files alt=\"") && content.contains("<file size=")) {
            api.getEventManager().executeEvent(new UserOtherFilesPingEvent(chat, user));
            return;
        }
    }

    public boolean isMe(JSONObject resource) {
        return (!resource.getJSONObject("resource").isNull("messagetype")) && (resource.getJSONObject("resource").getString("messagetype").startsWith("RichText/"));
    }
}
