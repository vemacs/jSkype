package xyz.gghost.jskype.internal.poller;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserStatusChangedEvent;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class OnlineStatusPoll implements PollRequest {
    private SkypeAPI api;

    public void process(JSONObject obj, Group chat) {
        xyz.gghost.jskype.user.OnlineStatus status = xyz.gghost.jskype.user.OnlineStatus.OFFLINE;

        if (obj.getString("status").equals("Online"))
            status = xyz.gghost.jskype.user.OnlineStatus.ONLINE;

        if (obj.getString("status").equals("Busy"))
            status = xyz.gghost.jskype.user.OnlineStatus.BUSY;

        try {
            User user = api.getUserByUsername(obj.getString("selfLink").split("/8:")[1].split("/")[0]);

            if (!user.getOnlineStatus().name().equals(status.name()))
                api.getEventManager().executeEvent(new UserStatusChangedEvent(user, status));

            UserImpl userImpl = (UserImpl)api.getUserByUsername(NamingUtils.getUsername("selfLink"));
            userImpl.setOnlineStatus(status);
            api.updateContact(userImpl);
        }catch(Exception e){
            //We came online
            api.s(status);
        }

    }

    public boolean isMe(JSONObject object){
        return ((!object.isNull("resourceType")) && object.getString("resourceType").equals("UserPresence"));
    }
}
