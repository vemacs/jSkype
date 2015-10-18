package xyz.gghost.jskype.internal.packet.requests;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.FailedToGetContactsException;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.RequestBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ghost on 10/10/2015.
 */
public class StartupRequests {
    private SkypeAPI api;
    public StartupRequests(SkypeAPI api){
        this.api = api;
    }

    public void setupContactsRealTime() throws FailedToGetContactsException, BadResponseException {
        List<User> contacts;

        HashMap<String, Boolean> blocked = new HashMap<String, Boolean>();
        ArrayList<String> usernames = new ArrayList<String>();

        RequestBuilder packet = new RequestBuilder(api);
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
                blocked.put(jsonLineItem.getString("id"), jsonLineItem.getBoolean("blocked"));
            }

            contacts = api.getSkypeInternals().getRequests().getUserMetaRequest().getUsers(usernames);

            if (contacts != null) {
                for (User user : contacts) {
                    ((UserImpl)user).setContact(true);
                    ((UserImpl)user).setBlocked(blocked.get(user.getUsername()));
                    api.updateContact(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setupRecent() throws AccountUnusableForRecentException {
        try {
            RequestBuilder packet = new RequestBuilder(api);
            packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=0&pageSize=200&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
            packet.setData("");
            packet.setType(RequestType.GET);
            String data = packet.makeRequest();

            if (data == null || data.equals(""))
                throw new AccountUnusableForRecentException();

            JSONArray jsonArray = new JSONObject(data).getJSONArray("conversations");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject recent = jsonArray.getJSONObject(i);
                String id = recent.getString("id");

                if (recent.getString("targetLink").contains("/contacts/8:")) {
                    api.updateGroup(new ContactGroupImpl(api, id));
                } else {
                    if (id.endsWith("@thread.skype")) {
                        try {
                            api.updateGroup(api.getSkypeInternals().getRequests().getGroupMetaRequest().getGroup(id));
                        } catch (Exception e) {
                            //you've been rate limited
                        }
                    }
                }
            }
        } catch (AccountUnusableForRecentException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
