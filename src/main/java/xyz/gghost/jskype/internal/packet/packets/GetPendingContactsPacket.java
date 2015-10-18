package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
import xyz.gghost.jskype.internal.packet.RequestBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.User;

import java.net.URLEncoder;
import java.util.ArrayList;

public class GetPendingContactsPacket {

    private SkypeAPI api;

    public GetPendingContactsPacket(SkypeAPI api) {
        this.api = api;
    }

    public ArrayList<User> getPending() throws NoPendingContactsException, BadResponseException {
        RequestBuilder packet = new RequestBuilder(api);
        packet.setType(RequestType.GET);
        packet.setUrl("https://api.skype.com/users/self/contacts/auth-request");
        String a = packet.makeRequest();
        if (a == null) {
            //api.getLogger().warning("Failed to get contact requests");
            throw new BadResponseException();
        } else {
            if (a.equals("")) {
                //api.getLogger().info("No contact requests available");
                throw new NoPendingContactsException();
            }
            ArrayList<User> pending = new ArrayList<User>();
            JSONArray json = new JSONArray(a);
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = json.getJSONObject(i);
                pending.add(api.getSkypeInternals().getRequests().getUserMetaRequest().getUser(object.getString("sender")));
            }
            return pending;
        }

    }

    public void acceptRequest(String user) {

        boolean canLog = api.isAllowLogging();
        api.setAllowLogging(false);
        String URL = "https://api.skype.com/users/self/contacts/auth-request/" + user + "/accept";
        RequestBuilder packet = new RequestBuilder(api);
        packet.setData("");
        packet.setUrl(URL);
        packet.setIsForm(true);
        packet.setType(RequestType.PUT);
        packet.makeRequest();


        RequestBuilder accept = new RequestBuilder(api);
        accept.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/8:" + user);
        accept.setIsForm(true);
        accept.setType(RequestType.PUT);
        accept.makeRequest();


        String URL2 = "https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/";
        RequestBuilder packet2 = new RequestBuilder(api);

        //TODO: Find a replacement for json.org that supports json building
        String data  = "{\"contacts\": [";
        boolean first = true;
        for (User usr : api.getContacts()){
            data = data + (!first ? "," : "");
            data = data + "{\"id\": \"" + usr.getUsername() + "\"}";
            first = false;
        }
        data = data + (!first ? "," : "");
        data = data + "{\"id\": \"" + user + "\"}";
        data = data  + "]}";packet2.setData(data);
        //end of json hackky code
        packet2.setUrl(URL2);
        packet2.setIsForm(true);
        packet2.setType(RequestType.POST);
        packet2.makeRequest();
        api.setAllowLogging(canLog);
    }

    public void acceptRequest(User usr) {
        acceptRequest(usr.getUsername());
    }

    public void sendRequest(String user){
        sendRequest(user, "Hi, I'd like to add you as a contact. -Sent from jSkypeAPI");
    }
    public void sendRequest(String user, String message){
        String URL = "https://api.skype.com/users/self/contacts/auth-request/" + user;
        RequestBuilder packet = new RequestBuilder(api);
        packet.setData("greeting=" + URLEncoder.encode(message));
        packet.setUrl(URL);
        packet.setIsForm(true);
        packet.setType(RequestType.PUT);
        packet.makeRequest();
    }
}
