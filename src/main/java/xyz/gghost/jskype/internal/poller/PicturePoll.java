package xyz.gghost.jskype.internal.poller;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.ChatPictureChangedEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.user.User;

public class PicturePoll implements PollRequest {
    private SkypeAPI api;

    public PicturePoll(SkypeAPI api){
        this.api = api;
    }

    public void process(JSONObject resource, Group chat) {
        String content = resource.getString("content");
        String newUrl = content.split("<value>URL@")[1].split("<")[0];        //TODO: jsoup
        String username = NamingUtils.getUsername(Jsoup.parse(content).getElementsByTag("initiator").get(0).text());
        User user = api.getUserByUsername(username);
        api.getEventManager().executeEvent(new ChatPictureChangedEvent(chat, user, newUrl));
    }

    public boolean isMe(JSONObject resource){
        return (!resource.getJSONObject("resource").isNull("messagetype") && resource.getJSONObject("resource").getString("messagetype").equals("ThreadActivity/PictureUpdate"));
    }
}
