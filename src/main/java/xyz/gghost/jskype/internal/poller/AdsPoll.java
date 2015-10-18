package xyz.gghost.jskype.internal.poller;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserNewMovieAdsPingEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class AdsPoll implements PollRequest {
    private SkypeAPI api;

    public void process(JSONObject obj, Group chat) {
        String id = obj.getString("content").split("om/pes/v1/items/")[1].split("\\\"")[0];
        String username = NamingUtils.getUsername(obj.getString("from"));
        User user = Poller.getUser(username, chat, api);
        api.getEventManager().executeEvent(new UserNewMovieAdsPingEvent(user, chat, id));
    }

    public boolean isMe(JSONObject name){
        return (!name.getJSONObject("resource").isNull("messagetype")) && (name.getJSONObject("resource").getString("messagetype").equals("RichText/Media_FlikMsg"));
    }
}
