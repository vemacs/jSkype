package xyz.gghost.jskype.internal.poller;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserChatEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class MessagePoll implements PollRequest {
    private SkypeAPI api;

    public void process(JSONObject resource, Group chat) {
        xyz.gghost.jskype.message.Message message = new xyz.gghost.jskype.message.Message();
        String username = NamingUtils.getUsername(resource.getString("from"));
        User user = Poller.getUser(username, chat, api);

        String content = "";

        if(!resource.isNull("content"))
            content = FormatUtils.decodeText(resource.getString("content"));

        if (!resource.isNull("clientmessageid"))
            message.setId(resource.getString("clientmessageid"));

        if (!resource.isNull("skypeeditedid")) {
            content = content.replaceFirst("Edited previous message: ", "").split("<e_m")[0];
            message.setId(resource.getString("skypeeditedid"));
            message.setEdited(true);
        }

        message.setSender(user);
        message.setTime(resource.getString("originalarrivaltime"));
        message.setUpdateUrl(resource.getString("resourceLink").split("/messages/")[0] + "/messages");
        message.setMessage(content);

        api.getEventManager().executeEvent(new UserChatEvent(chat, user, message));
    }

    public boolean isMe(JSONObject name){
        return (!name.getJSONObject("resource").isNull("messagetype")) && (name.getJSONObject("resource").getString("messagetype").equals("RichText") || name.getJSONObject("resource").getString("messagetype").equals("Text"));
    }
}
