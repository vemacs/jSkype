package xyz.gghost.jskype.internal.poller;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.TopicChangedEvent;
import xyz.gghost.jskype.events.UserJoinEvent;
import xyz.gghost.jskype.events.UserLeaveEvent;
import xyz.gghost.jskype.events.UserRoleChangedEvent;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

public class ThreadUpdatePoll implements PollRequest {

    private SkypeAPI api;
    public ThreadUpdatePoll(SkypeAPI api){
        this.api = api;
    }
    protected String type;
    public void process(JSONObject obj, Group chat) {
        switch (type){
            case "ThreadActivity/AddMember":
                for (Element a : Jsoup.parse(FormatUtils.decodeText(obj.getString("content"))).getElementsByTag("addmember")){
                    User adderUser = null;
                    User usernameUser = null;
                    if (a.getElementsByTag("initiator").size() > 0) {
                        String adder = NamingUtils.getUsername(a.getElementsByTag("initiator").get(0).text());
                        adderUser = api.getUserByUsername(adder);
                    }
                    if (a.getElementsByTag("target").size() > 0) {
                        String username = NamingUtils.getUsername(a.getElementsByTag("target").get(0).text());
                        usernameUser = api.getUserByUsername(username);
                    }
                    GroupUser user = new GroupUser(usernameUser, GroupUser.Role.USER, (GroupImpl)chat);
                    chat.getClients().add(user);
                    api.updateGroup(chat); //is this needed?
                    api.getEventManager().executeEvent(new UserJoinEvent(chat, usernameUser, adderUser));
                }
                break;
            case "ThreadActivity/DeleteMember":
                for (Element a : Jsoup.parse(FormatUtils.decodeText(obj.getString("content"))).getElementsByTag("deletemember")){
                    User removerUser = null;
                    GroupUser usernameUser = null;
                    if (a.getElementsByTag("initiator").size() > 0) {
                        String remover = NamingUtils.getUsername(a.getElementsByTag("initiator").get(0).text());
                        removerUser = chat.getUserByUsername(remover).getUser();
                    }
                    if (a.getElementsByTag("target").size() > 0) {
                        String username = NamingUtils.getUsername(a.getElementsByTag("target").get(0).text());
                        usernameUser = chat.getUserByUsername(username);
                    }
                    if (usernameUser != null)
                        chat.getClients().remove(usernameUser);
                    api.updateGroup(chat); //is this needed?
                    api.getEventManager().executeEvent(new UserLeaveEvent(chat, usernameUser.getUser(), removerUser));
                }
                break;
            case "ThreadActivity/RoleUpdate":
                for (Element a : Jsoup.parse(FormatUtils.decodeText(obj.getString("content"))).getElementsByTag("roleupdate")){
                    GroupUser updaterUser = null;
                    GroupUser usernameUser = null;
                    if (a.getElementsByTag("initiator").size() > 0) {
                        String updater = NamingUtils.getUsername(a.getElementsByTag("initiator").get(0).text());
                        updaterUser = chat.getUserByUsername(updater);
                    }
                    if (a.getElementsByTag("target").size() > 0) {
                        String username = NamingUtils.getUsername(a.getElementsByTag("target").get(0).getElementsByTag("id").get(0).text());
                        usernameUser = chat.getUserByUsername(username);
                    }
                    String role = a.getElementsByTag("role").get(0).text();
                    if (usernameUser != null) {
                        chat.getClients().remove(usernameUser);
                        GroupUser gu = new GroupUser(usernameUser.getUser(), role.equals("user") ? GroupUser.Role.USER : GroupUser.Role.MASTER,(GroupImpl)chat);
                        chat.getClients().add(gu);
                        api.updateGroup(chat); //is this needed?
                        api.getEventManager().executeEvent(new UserRoleChangedEvent(chat, usernameUser.getUser(), gu.role, updaterUser));
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean isMe(JSONObject resource){
        type = resource.getJSONObject("resource").isNull("messagetype") ? "" : resource.getJSONObject("resource").getString("messagetype");
        return (!resource.getJSONObject("resource").isNull("messagetype") && resource.getJSONObject("resource").getString("messagetype").startsWith("ThreadActivity/")) ;
    }

}
