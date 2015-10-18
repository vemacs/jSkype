package xyz.gghost.jskype.internal.packet.requests;

import org.json.JSONObject;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.Header;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.PacketBuilderUploader;
import xyz.gghost.jskype.internal.packet.RequestType;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;


public class PingPrepRequest {
    private SkypeAPI api;
    public PingPrepRequest(SkypeAPI api){
        this.api = api;
    }

    public String urlToId(String url, String groupId) {
        String id = getId();
        try {
            if (id == null) {
                api.log("Failed to get id");
                return null;
            }
            if (!allowRead(id, groupId)) {
                api.log("Failed to set perms");
                return null;
            }
            if (!writeData(id, new URL(url).openStream())) {
                api.log("Failed to set image data");
                return null;
            }
        } catch (Exception e) {}
        return id;
    }
    
    public String urlToId(File file, String groupId) {
        String id = getId();
        try {
            if (id == null) {
                api.log("Failed to get id");
                return null;
            }
            if (!allowRead(id, groupId)) {
                api.log("Failed to set perms");
                return null;
            }
            if (!writeData(id, new FileInputStream(file))) {
                api.log("Failed to set image data");
                return null;
            }
        } catch (Exception e) {}
        return id;
    }

    public String getId(){
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://api.asm.skype.com/v1/objects");
        packet.setData(" ");
        packet.setSendLoginHeaders(false); //Disable skype for web authentication
        packet.addHeader(new Header("Authorization", "skype_token " + api.getLoginTokens().getXToken()));  //Use the windows client login style
        packet.setType(RequestType.POST);
        String data = packet.makeRequest();
        if (data == null)
            return null;
        return new JSONObject(data).getString("id");
    }

    public boolean allowRead(String id, String longId){
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://api.asm.skype.com/v1/objects/" + id + "/permissions");
        packet.setData("{\"" + longId + "\":[\"read\"]}");
        packet.setSendLoginHeaders(false); //Disable skype for web authentication
        packet.addHeader(new Header("Authorization", "skype_token " + api.getLoginTokens().getXToken()));  //Use the windows client login style
        packet.setType(RequestType.PUT);
        String data = packet.makeRequest();
        return data != null;
    }

    public boolean writeData(String id, InputStream url){
        try {
            PacketBuilderUploader packet = new PacketBuilderUploader(api);
            packet.setUrl("https://api.asm.skype.com/v1/objects/" + id + "/content/imgpsh");
            packet.setSendLoginHeaders(false); //Disable skype for web authentication
            packet.setFile(true);
            packet.addHeader(new Header("Authorization", "skype_token " + api.getLoginTokens().getXToken())); //Use the windows client login style
            packet.setType(RequestType.PUT);
            String dataS = packet.makeRequest(url);

            if (dataS == null)
                return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
