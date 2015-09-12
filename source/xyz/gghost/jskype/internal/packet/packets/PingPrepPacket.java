package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;
import xyz.gghost.jskype.api.SkypeAPI;


import xyz.gghost.jskype.internal.packet.Header;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.PacketBuilderUploader;
import xyz.gghost.jskype.internal.packet.RequestType;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Ghost on 26/08/2015.
 */
public class PingPrepPacket {
    SkypeAPI api;
    public PingPrepPacket(SkypeAPI api){
        this.api = api;
    }

    public String urlToId(String url, String groupId, boolean group){
        String id = getId();
        if (id == null) {
            if (api.isDebugMode())
                System.out.println("Failed to get id");
            return null;
        }
        if (!allowRead(id, groupId, group)) {
            if (api.isDebugMode())
                System.out.println("Failed to set perms");
            return null;
        }
        if(!writeData(id, url)){
            if (api.isDebugMode())
                System.out.println("Failed to set image data");
            return null;
        }
        return id;
    }
    public String urlToId(File url, String groupId, boolean group){
        String id = getId();
        if (id == null) {
            if (api.isDebugMode())
                System.out.println("Failed to get id");
            return null;
        }
        if (!allowRead(id, groupId, group)) {
            if (api.isDebugMode())
                System.out.println("Failed to set perms");
            return null;
        }
        if(!writeData(id, url)){
            if (api.isDebugMode())
                System.out.println("Failed to set image data");
            return null;
        }
        return id;
    }

    public String getId(){
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://api.asm.skype.com/v1/objects");
        packet.setData(" ");
        packet.setSendLoginHeaders(false); //Disable skype for web authentication
        packet.addHeader(new Header("Authorization", "skype_token " + api.getSkype().getLoginTokens().getXToken()));  //Use the windows client login style
        packet.setType(RequestType.POST);
        String data = packet.makeRequest(api.getSkype());
        if (data == null)
            return null;
        return new JSONObject(data).getString("id");
    }
    public boolean allowRead(String id, String shortId, boolean group){
        PacketBuilder packet = new PacketBuilder(api);
        packet.setUrl("https://api.asm.skype.com/v1/objects/" + id + "/permissions");
        if (group) {
            packet.setData("{\"19:" + shortId + "@thread.skype\":[\"read\"]}");
        }else{
            packet.setData("{\"8:" + shortId + "\":[\"read\"]}");
        }
        packet.setSendLoginHeaders(false); //Disable skype for web authentication
        packet.addHeader(new Header("Authorization", "skype_token " + api.getSkype().getLoginTokens().getXToken()));  //Use the windows client login style
        packet.setType(RequestType.PUT);
        String data = packet.makeRequest(api.getSkype());
        return data != null;
    }
    public boolean writeData(String id, String url){
        try {

            URL image = new URL(url);
            InputStream data = image.openStream();

            PacketBuilderUploader packet = new PacketBuilderUploader(api);
            packet.setUrl("https://api.asm.skype.com/v1/objects/" + id + "/content/imgpsh");
            packet.setSendLoginHeaders(false); //Disable skype for web authentication
            packet.setFile(true);
            packet.addHeader(new Header("Authorization", "skype_token " + api.getSkype().getLoginTokens().getXToken())); //Use the windows client login style
            packet.setType(RequestType.PUT);

            String dataS = packet.makeRequest(api.getSkype(), data);
            if (dataS == null)
                return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean writeData(String id, File url){
        try {
            InputStream data = new FileInputStream(url);

            PacketBuilderUploader packet = new PacketBuilderUploader(api);

            packet.setUrl("https://api.asm.skype.com/v1/objects/" + id + "/content/imgpsh");
            packet.setSendLoginHeaders(false); //Disable skype for web authentication
            packet.setFile(true);
            packet.addHeader(new Header("Authorization", "skype_token " + api.getSkype().getLoginTokens().getXToken())); //Use the windows client login style
            packet.setType(RequestType.PUT);

            String dataS = packet.makeRequest(api.getSkype(), data);

            if (dataS == null)
                return false;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
