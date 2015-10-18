package xyz.gghost.jskype;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.event.EventManager;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
//import xyz.gghost.jskype.internal.calling.CallingMaster;
import xyz.gghost.jskype.internal.impl.SkypeInternals;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.PacketBuilderUploader;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.GetPendingContactsPacket;
import xyz.gghost.jskype.internal.auth.LoginTokens;
import xyz.gghost.jskype.user.LocalAccount;
import xyz.gghost.jskype.user.OnlineStatus;
import xyz.gghost.jskype.user.User;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkypeAPI {
    @Getter private List<Group> groups = new ArrayList<Group>();
    @Getter private List<User> contacts = new ArrayList<User>();
    @Getter private LoginTokens loginTokens = new LoginTokens();
    @Getter private EventManager eventManager = new EventManager();
    @Setter @Getter private boolean allowLogging = true;
    @Getter private String username;
    @Getter UUID uuid = UUID.randomUUID();
    @Getter private String password;
    @Getter @Setter private boolean loaded;
    @Getter private SkypeInternals skypeInternals = new SkypeInternals(this);
    @Getter @Setter private boolean debugMode = false;

    private OnlineStatus s = OnlineStatus.ONLINE;

    public SkypeAPI(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Login to skype
     * @return "Builder"
     * @throws Exception Failed to login/badusernamepassword exception
     */
    public SkypeAPI login() throws Exception {
        skypeInternals.login();
        return this;
    }

    /**
     * Make shift logger... rly bad. Just System.out.println if SkypeAPI#allowLogging is true
     */
    public void log(String msg) {
        if (allowLogging)
            System.out.println(msg);
    }


    /**
     * Attempt to stop all skype threads
     * @return
     */
    public void stop() {
        skypeInternals.stop();
    }

    /**
     * Get your current online status
     * @return
     */
    public OnlineStatus getOnlineStatus() {
        return s;
    }

    /**
     * Do not use
     * @param status
     */
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
        return user != null ? user : skypeInternals.getRequests().getUserMetaRequest().getUser(username);
    }
    /**
     * Get contact by username
     */
    public User getContact(String username) {
        for (User contact : getContacts())
            if (contact.getUsername().equalsIgnoreCase(username))
                return contact;
        return null;
    }

    /**
     * Get the group by shorter id
     * @param shortId
     * @return null if the api hasn't loaded or the group is unknown to us
     */
    public Group getGroupById(String shortId){
        for (Group group : groups) {
            if (group.getId().equals(shortId))
                return group;
        }
        return null;
    }

    /**
     * Set your online status
     * @param a
     */
    public void updateStatus(OnlineStatus a){
        PacketBuilder packet = new PacketBuilder(this);
        packet.setData("{\"status\":\"" +  Character.toString(a.name().charAt(0)).toUpperCase() + (a.name().substring(1).toLowerCase()) +"\"}");
        packet.setType(RequestType.PUT);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService");
        packet.makeRequest();
    }
    /**
     * Update a group object in the recent array
     */
    public void updateGroup(Group group){
        List<Group> oldGroups = new ArrayList<Group>();
        for (Group groupA : groups)
            if (groupA.getId().equals(group.getId()))
                oldGroups.add(groupA);
        getGroups().removeAll(oldGroups);
        getGroups().add(group);
    }
    /**
     * Update a User object in the contacts array
     */
    public void updateContact(User newUser){
        List<User> oldUsers = new ArrayList<User>();
        for (User user : getContacts())
            if (user.getUsername().equals(newUser.getUsername()))
                oldUsers.add(user);
        getContacts().removeAll(oldUsers);
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
            return new ArrayList<>();

        JSONArray jsonArray = new JSONArray(data);
        ArrayList<String> usernames = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject contact = jsonArray.getJSONObject(i);
            usernames.add(contact.getJSONObject("ContactCards").getJSONObject("Skype").getString("SkypeName"));
        }
        return getSkypeInternals().getRequests().getUserMetaRequest().getUsers(usernames);
    }
    /**
     * Get user info about the account
     */
    public LocalAccount getAccountInfo(){
       return getSkypeInternals().getRequests().getUserMetaRequest().getMe();
    }
    /**
     * Change profile picture
     */
    public void changePictureFromFile(String url){
        try {
            //No point of making a new class just for this one small method
            PacketBuilderUploader uploader = new PacketBuilderUploader(this);
            uploader.setSendLoginHeaders(true);
            uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
            uploader.setType(RequestType.PUT);
            uploader.makeRequest(new FileInputStream(url));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Update your profile picture from a url
     */
    public void changePictureFromUrl(String url){
        try {
            //No point of making a new class just for this one small method
            PacketBuilderUploader uploader = new PacketBuilderUploader(this);
            uploader.setSendLoginHeaders(true);
            uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
            uploader.setType(RequestType.PUT);
            URL image = new URL(url);
            InputStream data = image.openStream();
            uploader.makeRequest(data);
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    /**
     * Create a new group
     */
    public Group createNewGroup(){
        JSONObject json = new JSONObject()
                .put("members", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "8:" + getUsername())
                                        .put("role", "Admin")));

        PacketBuilder buildGroup = new PacketBuilder(this);
        buildGroup.setData(json.toString());
        buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
        buildGroup.setType(RequestType.POST);
        buildGroup.makeRequest();

        String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
        updateGroupName(idLong);

        return skypeInternals.getRequests().getGroupMetaRequest().getGroup(idLong);
    }

    /**
     * Attempts to make a group with users... might not work
     */
    public Group createNewGroupWithUsers(ArrayList<String> users){
        JSONArray members = new JSONArray()
                .put(new JSONObject()
                        .put("id", "8:" + getUsername())
                        .put("role", "Admin"));

        for (String user : users)
            members.put(new JSONObject()
                    .put("id", "8:" + user)
                    .put("role", "User"));

        JSONObject json = new JSONObject()
                .put("members", members);

        PacketBuilder buildGroup = new PacketBuilder(this);
        buildGroup.setData(json.toString());
        buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
        buildGroup.setType(RequestType.POST);
        buildGroup.makeRequest();

        String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
        updateGroupName(idLong);

        return skypeInternals.getRequests().getGroupMetaRequest().getGroup(idLong);
    }

    private void updateGroupName(String idLong){
        PacketBuilder pb = new PacketBuilder(this);
        pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + idLong + "/properties?name=topic");
        pb.setType(RequestType.PUT);
        pb.setData(new JSONObject().put("topic", "New Group").toString());
        pb.makeRequest();
    }
    /**
     * Join a group from a skype invite link
     */
    public void joinInviteLink(String url){
        PacketBuilder getId = new PacketBuilder(this);
        System.out.println(url);
        getId.setUrl("https://join.skype.com/api/v1/meetings/" + url.split(".com/")[1]);
        getId.setType(RequestType.GET);
        String a = getId.makeRequest();
        if (a == null)
            return;
        PacketBuilder getLongId = new PacketBuilder(this);
        getLongId.setUrl("https://api.scheduler.skype.com/conversation/" + new JSONObject(a).get("longId"));
        getLongId.setType(RequestType.GET);
        String b = getLongId.makeRequest();
        if (b == null)
            return;
        reJoinGroup(new JSONObject(b).getString("ThreadId"));

    }

    /**
     * Kicked and the group is still joionable? Use this method!
     */
    public void reJoinGroup(Group group){
        reJoinGroup(group.getLongId());
    }

    /**
     * Join a joinable group from it's long id
     * @param longId
     */
    public void reJoinGroup(String longId) {
        skypeInternals.getRequests().getUserRankingRequest().addUser(longId, getUsername());
    }
    /**
     * Get contact requests
     * @return
     * @throws BadResponseException
     * @throws NoPendingContactsException
     */
    public ArrayList<User> getContactRequests() throws BadResponseException, NoPendingContactsException {
        return new GetPendingContactsPacket(this).getPending();
    }
    public void acceptContactRequest(String username){
        new GetPendingContactsPacket(this).acceptRequest(username);
    }

}
