package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.FailedToGetContactsException;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.var.User;

import java.util.ArrayList;

public class GetContactsPacket {

    private SkypeAPI api;
    private Skype usr;

    public GetContactsPacket(SkypeAPI api, Skype usr) {
        this.api = api;
        this.usr = usr;
    }
    public void setupContact() throws FailedToGetContactsException, BadResponseException {
        if (api.isDebugMode())
            System.out.println("Updating contacts!");
        ArrayList<User> contacts = new ArrayList<User>();
        ArrayList<String> usernames = new ArrayList<String>();

        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://contacts.skype.com/contacts/v1/users/" + usr.getUsername().toLowerCase() + "/contacts?filter=contacts");
        packet.setType(RequestType.OPTIONS);
        packet.getData();
        packet.setType(RequestType.GET);

        String a = packet.makeRequest(usr);

        if (a == null) {

            if (api.isDebugMode()) {
                System.out.println("Failed to request Skype for your contacts.");
                System.out.println("Code: " + packet.getCode() + "\nData: " + packet.getData() + "\nURL: " + packet.getUrl());
            }

            if (usr.getContacts().size() == 0)
                usr.getContacts().add(api.getSkype().getSimpleUser(usr.getUsername()));

            return;

        }
        try {
            JSONObject jsonObject = new JSONObject(a);
            JSONArray lineItems = jsonObject.getJSONArray("contacts");
            for (Object o : lineItems) {
                JSONObject jsonLineItem = (JSONObject) o;
                usernames.add(jsonLineItem.getString("id"));
            }
            contacts = new GetProfilePacket(api, usr).getUsers(usernames);
            if (contacts != null) {
                ArrayList<User> value = new ArrayList<User>();
                for (User user : contacts) {
                    user.setContact(true);
                    usr.getContacts().add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

