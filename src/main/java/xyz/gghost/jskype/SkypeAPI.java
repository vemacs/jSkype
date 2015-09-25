package xyz.gghost.jskype;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.event.EventManager;
import xyz.gghost.jskype.internal.auth.Auth;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
//import xyz.gghost.jskype.internal.calling.CallingMaster;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.PacketBuilderUploader;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.GetPendingContactsPacket;
import xyz.gghost.jskype.internal.packet.packets.GetProfilePacket;
import xyz.gghost.jskype.internal.auth.LoginTokens;
import xyz.gghost.jskype.internal.threads.*;
import xyz.gghost.jskype.user.LocalAccount;
import xyz.gghost.jskype.user.OnlineStatus;
import xyz.gghost.jskype.user.User;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SkypeAPI {

    @Getter
    private List<Group> groups = new ArrayList<Group>();
    @Getter
    private List<User> contacts = new ArrayList<User>();
    @Getter
    private LoginTokens loginTokens = new LoginTokens();
    @Getter
    private EventManager eventManager = new EventManager();
    @Getter
    private HashMap<String, MessageHistory> a = new HashMap<String, MessageHistory>();  //Could use an interface to hide this but its not worth it
    @Setter
    @Getter
    private boolean allowLogging = true;
    @Getter
    private String username;
    @Getter
    UUID uuid = UUID.randomUUID();
    @Getter
    private String password;
    @Getter
    @Setter
    private boolean loaded;
    private OnlineStatus s = OnlineStatus.ONLINE;
    private Poller poller;
    private Thread contactUpdater;
    private Thread pinger;
    private ConvoUpdater convoUpdater;
    private PendingContactEventThread pendingContactThread;
    @Getter @Setter private boolean reloggin = false;
//    @Getter private CallingMaster callingMaster = new CallingMaster();
    public SkypeAPI(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void login() throws Exception {
        new Auth().login(this);
        reloggin = true;
        init();
        updateStatus(OnlineStatus.ONLINE);
    }

    public void log(String msg) {
        //TODO: custom logger
        if (allowLogging)
            System.out.println(msg);
    }

    private void init() {
        pinger = new Ping(this);
        pinger.start();
        contactUpdater = new ContactUpdater(this);
        contactUpdater.start();
        pendingContactThread = new PendingContactEventThread(this);
        pendingContactThread.start();
        poller = new Poller(this);
        poller.start();
        convoUpdater = new ConvoUpdater(this);
        convoUpdater.start();
    }

    public void stop() {
        poller.stopThreads();
        pinger.stop();
        contactUpdater.stop();
        poller.stop();
        convoUpdater.stop();
        pendingContactThread.stop();
    }

    public OnlineStatus getOnlineStatus() {
        return s;
    }
    //TODO: interface
    public void s(OnlineStatus status) {
        s = status;
    }
    /**
     * This method will get as much data as possible about a user without contacting to skype
     */
    public User getSimpleUser(String username) {
        User user = getContact(username);
        return user != null ? user : new UserImpl(username);
    }
    /**
     * get user by username
     */
    public User getUserByUsername(String username) {
        User user = getContact(username);
        return user != null ? user : new GetProfilePacket(this).getUser(username);
    }
    /**
     * Get contact by username
     */
    public User getContact(String username) {
        for (User contact : getContacts()) {
            if (contact.getUsername().equalsIgnoreCase(username))
                return contact;
        }
        return null;
    }
    public Group getGroupById(String shortId){
        for (Group group : groups) {
            if (group.getId().equals(shortId))
                return group;
        }
        return null;
    }
    public void updateStatus(OnlineStatus a){
        PacketBuilder packet = new PacketBuilder(this);
        packet.setData("{\"status\":\"" +  Character.toString(a.name().charAt(0)).toUpperCase() + (a.name().substring(1).toLowerCase()) +"\"}");
        packet.setType(RequestType.PUT);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService");
        packet.makeRequest();
    }
    public void updateGroup(Group group){
        Group oldGroup = null;
        for (Group groupA : groups){
            if (groupA.getId().equals(group.getId()))
                oldGroup = groupA;
        }
        if (oldGroup != null)
            getGroups().remove(oldGroup);
        getGroups().add(group);
    }
    public void updateContact(User newUser){
        User oldUser = null;
        for (User user : getContacts()){
            if (user.getUsername().equals(newUser.getUsername()))
                oldUser = user;
        }
        if (oldUser != null)
            getContacts().remove(oldUser);
        getContacts().add(newUser);
    }
    /**
     * Attempts to send a contact request
     */
    public void sendContactRequest(String username) {
        new GetPendingContactsPacket(this).sendRequest(username);
    }

    /**
     * Attempts to send a contact request with a custom greeting
     */
    public void sendContactRequest(String username, String greeting) {
        new GetPendingContactsPacket(this).sendRequest(username, greeting);
    }
    /**
     * Skype db lookup / search
     */
    public List<User> searchSkypeDB(String keywords){
        PacketBuilder packet = new PacketBuilder(this);
        packet.setType(RequestType.GET);
        packet.setUrl("https://api.skype.com/search/users/any?keyWord=" + URLEncoder.encode(keywords)+ "&contactTypes[]=skype");
        String data = packet.makeRequest();
        if (data == null)
            return null;

        JSONArray jsonArray = new JSONArray(data);
        ArrayList<String> usernames = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject contact = jsonArray.getJSONObject(i);
            usernames.add(contact.getJSONObject("ContactCards").getJSONObject("Skype").getString("SkypeName"));
        }

        return new GetProfilePacket(this).getUsers(usernames);
    }
    /**
     * Get user info about the account
     */
    public LocalAccount getAccountInfo(){
        return new GetProfilePacket(this).getMe();
    }
    /**
     * Change profile picture
     */
    public void changePictureFromFile(String url){
        try {
            //No point of making a new class just for this one small method
            PacketBuilderUploader uploader = new PacketBuilderUploader(this);
            uploader.setSendLoginHeaders(true);
            uploader.setUrl("https://api.skype.com/users/itsghostbot/profile/avatar");
            uploader.setType(RequestType.PUT);
            uploader.makeRequest(new FileInputStream(url));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void changePictureFromUrl(String url){
        try {
            //No point of making a new class just for this one small method
            PacketBuilderUploader uploader = new PacketBuilderUploader(this);
            uploader.setSendLoginHeaders(true);
            uploader.setUrl("https://api.skype.com/users/itsghostbot/profile/avatar");
            uploader.setType(RequestType.PUT);
            URL image = new URL(url);
            InputStream data = image.openStream();
            uploader.makeRequest(data);
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public ArrayList<User> getContactRequests() throws BadResponseException, NoPendingContactsException {
        return new GetPendingContactsPacket(this).getPending();
    }

}
