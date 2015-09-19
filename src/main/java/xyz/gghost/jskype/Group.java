package xyz.gghost.jskype;

import xyz.gghost.jskype.internal.message.Message;
import xyz.gghost.jskype.internal.message.MessageHistory;
import xyz.gghost.jskype.internal.user.GroupUser;

import java.io.File;
import java.net.URL;
import java.util.List;

public interface Group {
    void kick(String usr);
    void add(String usr);
    void leave();
    MessageHistory getMessageHistory();
    String getId();
    String getPictureUrl();
    String getTopic();
    String getLongId();
    Message sendMessage(Message msg);
    Message sendMessage(String msg);
    Message sendImage(File url);
    Message sendImage(URL url);
    List<GroupUser> getClients();
    boolean isUserChat();
}
