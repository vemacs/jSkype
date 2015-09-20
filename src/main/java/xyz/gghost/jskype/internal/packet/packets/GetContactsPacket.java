package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import org.json.JSONArray;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.FailedToGetContactsException;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.List;

public class GetContactsPacket {

    private final SkypeAPI api;

    public GetContactsPacket(SkypeAPI api) {
        this.api = api;
    }
    public void setupContact() throws FailedToGetContactsException, BadResponseException {

        List<User> contacts;
        List<String> usernames = new ArrayList<String>();

        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://contacts.skype.com/contacts/v1/users/" + api.getUsername().toLowerCase() + "/contacts?filter=contacts");
        packet.setType(RequestType.OPTIONS);
        packet.getData();
        packet.setType(RequestType.GET);
        String a = packet.makeRequest();
        if (a == null) {
            api.log("Failed to request Skype for your contacts.");
            api.log("Code: " + packet.getCode() + "\nData: " + packet.getData() + "\nURL: " + packet.getUrl());

            if (api.getContacts().size() == 0)
                api.getContacts().add(api.getSimpleUser(api.getUsername()));
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(a);
            JSONArray lineItems = jsonObject.getJSONArray("contacts");
            for (Object o : lineItems) {
                JSONObject jsonLineItem = (JSONObject) o;
                usernames.add(jsonLineItem.getString("id"));
            }
            contacts = new GetProfilePacket(api).getUsers(usernames);
            if (contacts != null) {
                for (User user : contacts) {
                    user.setContact(true);
                    api.updateContact(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

