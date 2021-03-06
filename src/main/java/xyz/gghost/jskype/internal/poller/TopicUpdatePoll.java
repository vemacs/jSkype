package xyz.gghost.jskype.internal.poller;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.TopicChangedEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class TopicUpdatePoll implements PollRequest {
    private SkypeAPI api;

    public void process(JSONObject obj, Group chat) {
        String topic = "";
        Document htmlVals = Jsoup.parse(obj.getString("content"));

        try {
            topic = FormatUtils.decodeText(htmlVals.getElementsByTag("value").get(0).text());
        }catch(Exception e){
            //If we fail to get the topic, use the usernames of group users
            for (GroupUser user : chat.getClients())
                topic = topic + ", " + user.getUser().getUsername();
            topic = topic.replaceFirst(", ", "");
        }

        String username = NamingUtils.getUsername(htmlVals.getElementsByTag("initiator").get(0).text());
        String oldTopic = api.getGroupById(chat.getId()).getTopic();

        User user = Poller.getUser(username, chat, api);
        api.getEventManager().executeEvent(new TopicChangedEvent(chat, user, topic, oldTopic));

        ((GroupImpl)chat).setTopic(topic);
    }

    public boolean isMe(JSONObject resource){
        return (!resource.getJSONObject("resource").isNull("messagetype") && resource.getJSONObject("resource").getString("messagetype").equals("ThreadActivity/TopicUpdate")) ;
    }
}
