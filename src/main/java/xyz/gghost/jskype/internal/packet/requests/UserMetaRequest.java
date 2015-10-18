package xyz.gghost.jskype.internal.packet.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.impl.LocalAccountImpl;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.utils.NamingUtils;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.LocalAccount;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ghost on 10/10/2015.
 */
public class UserMetaRequest {
    private SkypeAPI api;
    /*
        TODO: recode
     */
    public UserMetaRequest(SkypeAPI api){
        this.api = api;
    }

    public LocalAccount getMe() {
        LocalAccountImpl me = new LocalAccountImpl();

        PacketBuilder admin = new PacketBuilder(api);
        admin.setType(RequestType.GET);
        admin.setUrl("https://api.skype.com/users/self/");
        String adminData = admin.makeRequest();

        if (adminData != null) {
            JSONObject jsonA = new JSONObject(adminData);
            if (!jsonA.isNull("registrationIp"))
                me.setFirstLoginIP(jsonA.getString("registrationIp"));
            if (!jsonA.isNull("registrationDate"))
                me.setCreationTime(jsonA.getString("registrationDate"));
            if (!jsonA.isNull("language"))
                me.setLanguage(jsonA.getString("language"));
            if (!jsonA.isNull("email"))
                me.setEmail(jsonA.getString("email"));
        }

        PacketBuilder profile = new PacketBuilder(api);
        profile.setType(RequestType.GET);
        profile.setUrl("https://api.skype.com/users/self/profile/");
        String profileData = profile.makeRequest();

        if (profileData == null)
            return me;

        JSONObject json = new JSONObject(profileData);
        if (!json.isNull("jobtitle"))
            me.setMicrosoftRank(json.getString("jobtitle"));
        if (!json.isNull("homepage"))
            me.setSite(json.getString("homepage"));
        if (!json.isNull("avatarUrl"))
            me.setAvatar(json.getString("avatarUrl"));
        if (!json.isNull("birthday"))
            me.setDOB(json.getString("birthday"));
        if (!json.isNull("firstname"))
            me.setName(json.getString("firstname"));
        if (!json.isNull("firstname"))
            me.setDisplayName(json.getString("firstname")); //TODO: fix
        if (!json.isNull("mood"))
            me.setMood(FormatUtils.decodeText(json.isNull("richMood") ? (json.isNull("mood") ? "" : json.getString("mood")) : json.getString("richMood")));
        if (!json.isNull("phoneOffice"))
            me.setPhoneNumber(json.getString("phoneOffice"));
        if (!json.isNull("phoneHome"))
            me.setPhoneNumber(json.getString("phoneHome"));
        if (!json.isNull("phoneMobile"))
            me.setPhoneNumber(json.getString("phoneMobile"));

        if (!json.isNull("city"))
            me.setLocation(json.getString("city"));
        if (!json.isNull("country"))
            me.setLocation(me.getLocation() + ", " + json.getString("country"));

        if (me.getLocation().startsWith(", "))
            me.setLocation(me.getLocation().replaceFirst(", ", ""));

        return me;
    }

    public User getUser(String username) {
        if(username.equalsIgnoreCase("echo123"))
            return minorUserData(username);

        PacketBuilder packet = new PacketBuilder(api);

        packet.setType(RequestType.POST);
        packet.setUrl("https://api.skype.com/users/self/contacts/profiles");
        packet.setData("contacts[]=" + username);
        packet.setIsForm(true);

        String data =  packet.makeRequest();

        if (data == null) {
            api.log("\nFailed to get profile of " + username  + " due to an internal server error");
            api.log("Someone may have blocked you or have high privacy settings.");
            api.log("You can ignore this...");
            return minorUserData(username);
        }

        try {
            UserImpl user = new UserImpl(username);

            data = data.replaceFirst("\\[", "").replace("]", "");
            JSONObject jsonObject = new JSONObject(data); //ln 50

            user.setUsername(username);
            user.setPictureUrl(jsonObject.isNull("avatarUrl") ? "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png" : jsonObject.getString("avatarUrl"));
            user.setDisplayName(jsonObject.isNull("displayname") ? (jsonObject.isNull("firstname") ? username : getDisplayName(data)) : jsonObject.getString("displayname"));
            user.setMood(jsonObject.isNull("richMood") ? (jsonObject.isNull("mood") ? "" : jsonObject.getString("mood")) : jsonObject.getString("richMood"));
            user.setMood(FormatUtils.decodeText(user.getMood()));

            return user;
        }catch(Exception e){
            api.log("Failed to get profile of " + username);
            api.log("Data : " + data);
            e.printStackTrace();
            return minorUserData(username);
        }
    }
    public List<User> getUsers(List<String> usernames) {
        List<User> contacts = new ArrayList<User>();
        PacketBuilder packet = new PacketBuilder(api);

        packet.setType(RequestType.POST);
        packet.setUrl("https://api.skype.com/users/self/contacts/profiles");
        packet.setData("contacts[]=");

        boolean first = true;

        for (String username : usernames) {
            if (!username.equals("echo123"))
                packet.setData( packet.getData() + (first ? "" : "&contacts[]=") + username);
            first = false;
        }

        packet.setIsForm(true);

        String data =  packet.makeRequest();
        if (data == null)
            return null;

        try {
            JSONArray jsonObject = new JSONArray(data);
            int count = 0;                                                   //offset for displayname grabber
            for (int ii = 0; ii < jsonObject.length(); ii++) {
                JSONObject jData = jsonObject.getJSONObject(ii);
                count ++;                                                    // ++ 1

                UserImpl user = new UserImpl(jData.getString("username"));

                user.setPictureUrl(jData.isNull("avatarUrl") ? "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png" : jData.getString("avatarUrl"));
                user.setDisplayName(jData.isNull("displayname") ? (jData.isNull("firstname") ? jData.getString("username") : getDisplayName(data, (count))) : jData.getString("displayname"));
                user.setMood(jData.isNull("richMood") ? (jData.isNull("mood") ? "" : jData.getString("mood")) : jData.getString("richMood"));
                user.setMood(FormatUtils.decodeText(user.getMood()));

                user.setFirstName(jData.isNull("firstname") ? "" : jData.getString("firstname"));
                user.setLastName(jData.isNull("lastname") ? "" : jData.getString("lastname"));

                contacts.add(user);
            }

            return contacts;
        } catch(Exception e) {
            api.log("Failed to get profile of arraylist");
            api.log("Data : " + data);
            e.printStackTrace();
            return null;
        }
    }

    private User minorUserData(String username){
        return new UserImpl(username);
    }

    public String getDisplayName(String data, int count){
        return (data.split("firstname\":")[count].split("\",\"")[0]).replace("\"", "");
    }
    public String getDisplayName(String data){
        return data.split("firstname\":\"")[1].split("\",\"")[0];
    }
}
