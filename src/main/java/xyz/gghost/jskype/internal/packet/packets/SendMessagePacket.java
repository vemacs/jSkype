package xyz.gghost.jskype.internal.packet.packets;


import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class SendMessagePacket {

    private SkypeAPI api;

    public SendMessagePacket(SkypeAPI api) {
        this.api = api;
    }

    public Message editMessage(Message msg, String edit) {
        PacketBuilder packet = new PacketBuilder(api);
        packet.setType(RequestType.POST);

        msg.setMessage(edit);

        if (edit == null || msg == null)
            return msg;

        packet.setData("{\"content\":\"" + edit.replace("\"", "\\\"") + "\",\"messagetype\":\"RichText\",\"contenttype\":\"text\",\"skypeeditedid\":\"" + msg.getId() + "\"}");

        packet.setUrl(msg.getUpdateUrl());
        packet.makeRequest();

        return msg;
    }

    public Message sendPing(String longId, Message msg, String ids) {
        String id = String.valueOf(System.currentTimeMillis());
        String url = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages";
        msg.setSender(api.getSimpleUser(api.getUsername()));
        msg.setUpdateUrl(url);
        msg.setTime(id);
        msg.setId(id);

        PacketBuilder packet = new PacketBuilder(api);
        packet.setType(RequestType.POST);
        String data = "{content: \"<URIObject type=\\\"Picture.1\\\" uri=\\\"https://api.asm.skype.com/v1/objects/" + ids + "\\\" url_thumbnail=\\\"https://api.asm.skype.com/v1/objects/"+ ids + "/views/imgt1\\\">To view this shared photo, go to: <a href=\\\"https://api.asm.skype.com/s/i?" + ids + "\\\">https://api.asm.skype.com/s/i?" + ids + "<\\/a><OriginalName v=\\\"^005CFF2010F86CC63570CA528D9B2CCFE3BF3B54DF8A01E92E^pimgpsh_thumbnail_win_distr.jpg\\\"/><meta type=\\\"photo\\\" originalName=\\\"^005CFF2010F86CC63570CA528D9B2CCFE3BF3B54DF8A01E92E^pimgpsh_thumbnail_win_distr.jpg\\\"/><\\/URIObject>\", messagetype: \"RichText/UriObject\", contenttype: \"text\", clientmessageid: \"" + id + "\"}";
        packet.setData(data);
        packet.setUrl(url);
        packet.makeRequest();

        return msg;
    }

    public Message sendMessage(String longId, Message msg) {

        String id = String.valueOf(System.currentTimeMillis());
        String url = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages";

        msg.setSender(api.getSimpleUser(api.getUsername()));
        msg.setUpdateUrl(url);
        msg.setTime(id);
        msg.setId(id);

        PacketBuilder packet = new PacketBuilder(api);
        packet.setType(RequestType.POST);
        packet.setData("{\"content\":\"" + msg.getMessage().replace("\"", "\\\"") + "\",\"messagetype\":\"RichText\",\"contenttype\":\"text\",\"clientmessageid\":\"" + id + "\"}");

        packet.setUrl(url);
        packet.makeRequest();

        return msg;
    }

}
