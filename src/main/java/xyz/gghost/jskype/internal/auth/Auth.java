package xyz.gghost.jskype.internal.auth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserRecaptchaEvent;
import xyz.gghost.jskype.exception.*;
import xyz.gghost.jskype.internal.packet.Header;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Auth {

    private String url = "";
    private PacketBuilder packet;

    private Document postData(String username, String password, SkypeAPI api, String id, String answer, String token) throws BadResponseException {
        Date now = new Date();

        PacketBuilder getIdsPacket = new PacketBuilder(api);
        getIdsPacket.setSendLoginHeaders(false);
        getIdsPacket.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");
        getIdsPacket.setType(RequestType.GET);

        String htmlIds = getIdsPacket.makeRequest();
        if (htmlIds == null)
            throw new BadResponseException();
        String pie = htmlIds.split("name=\"pie\" id=\"pie\" value=\"")[1].split("\"")[0];
        String etm = htmlIds.split("name=\"etm\" id=\"etm\" value=\"")[1].split("\"")[0];

        StringBuilder data = new StringBuilder();
        data.append("username=").append(URLEncoder.encode(username));
        data.append("&password=").append(URLEncoder.encode(password));
        data.append("&timezone_field=").append(URLEncoder.encode(new SimpleDateFormat("XXX").format(now).replace(':', '|')));
        data.append("&js_time=").append(String.valueOf(now.getTime() / 1000));
        data.append("&pie=").append(URLEncoder.encode(pie));
        data.append("&etm=").append(URLEncoder.encode(etm));
        data.append("&client_id=").append(URLEncoder.encode("578134"));
        data.append("&redirect_uri=").append(URLEncoder.encode("https://web.skype.com"));

        if (id != null) {
            data.append("&hip_solution=").append(URLEncoder.encode(answer));
            data.append("&hip_token=").append(URLEncoder.encode(token));
            data.append("&hip_type=").append(URLEncoder.encode("visual"));
            data.append("&fid=").append(URLEncoder.encode(id));
            data.append("&captcha_provider=").append(URLEncoder.encode("Hip"));
        }

        String formData = data.toString();

        PacketBuilder login = new PacketBuilder(api);
        login.setSendLoginHeaders(false);
        login.setType(RequestType.POST);
        login.setIsForm(true);
        login.setData(formData);
        login.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");

        String html = login.makeRequest();
        if (html == null)
            throw new BadResponseException();

        return Jsoup.parse(html);

    }

    public void login(SkypeAPI api) throws Exception {
        Document loginResponse = postData(api.getUsername(), api.getPassword(), api, null, null, null);
        handle(loginResponse, api);
        try {
            prepare(api);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public void handle(Document loginResponseDocument, SkypeAPI account) throws Exception {
        try {
            Elements inputs = loginResponseDocument.select("input[name=skypetoken]");
            if (inputs.size() > 0) {
                account.getLoginTokens().setXToken(inputs.get(0).attr("value"));
            } else if (loginResponseDocument.html().contains("var skypeHipUrl = \"https://clien")) {
                account.log("Failed to connect due to a recaptcha!");

                String url = loginResponseDocument.html().split("var skypeHipUrl = \"")[1].split("\";")[0];

                PacketBuilder pb = new PacketBuilder(account);
                pb.setSendLoginHeaders(false);
                pb.setUrl(url);
                pb.setType(RequestType.GET);
                String imgUrl = pb.makeRequest();

                String imageUrl2 = imgUrl.split("imageurl:'")[1].split("',")[0];

                UserRecaptchaEvent event = new UserRecaptchaEvent(imageUrl2, account.getUsername());
                account.getEventManager().executeEvent(event);

                String token = imgUrl.split("hid=")[1].split("&fid")[0];
                String fid = url.split("&fid=")[1].split("&")[0];

                try {
                    Document loginResponse = postData(account.getUsername(), account.getPassword(), account, fid, event.getAnswer(), token);
                    handle(loginResponse, account);
                    prepare(account);
                }catch(Exception e){
                    throw e;
                }

                return;
            } else {
                Elements elements = loginResponseDocument.select(".message_error");
                if (elements.size() > 0) {
                    Element div = elements.get(0);
                    if (div.children().size() > 1) {
                        Element span = div.child(1);

                        throw new FailedToLoginException(span.text());
                    }
                }
                throw new FailedToLoginException("Could not find error message. Dumping entire page. \n" + loginResponseDocument.html());
            }
        }catch (FailedToLoginException  e){
            throw e;
        }
    }



    public void prepare(SkypeAPI api) throws BadUsernamePasswordException, FailedToLoginException{
        authLogin(api);
        if (!reg(api)) {
            api.log("Failed to get update data from skype due to a login error... Attempting to relogin, however this wont work until the auto pinger kicks in.");
            authLogin(api);
            try {
                Thread.sleep(1750);
                prepare(api);
            } catch (InterruptedException ignored) {}
        }
        save(api);
    }


    public void authLogin(SkypeAPI api) throws FailedToLoginException {
        url = location(api).split("://")[1].split("/")[0];
        api.getLoginTokens().setReg(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[0]);
        api.getLoginTokens().setEndPoint(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[2].split("=")[1]);
    }

    public boolean save(SkypeAPI api) {
        String id = "{\"id\":\"messagingService\",\"type\":\"EndpointPresenceDoc\",\"selfLink\":\"uri\",\"publicInfo\":{\"capabilities\":\"video|audio\",\"type\":\"1\",\"skypeNameVersion\":\"skype.com\",\"nodeInfo\":\"2\",\"version\":\"2\"},\"privateInfo\":{\"epname\":\"Skype\"}}";
        PacketBuilder packet = new PacketBuilder(api);
        packet.setType(RequestType.PUT);
        packet.setData(id);
        packet.setUrl("https://" + url + "/v1/users/ME/endpoints/" + api.getLoginTokens().getEndPoint() + "/presenceDocs/messagingService");
        return packet.makeRequest() != null;
    }

    public boolean reg(SkypeAPI api) {
        PacketBuilder packet = new PacketBuilder(api);
        String id = "{\"channelType\":\"httpLongPoll\",\"template\":\"raw\",\"interestedResources\":[\"/v1/users/ME/conversations/ALL/properties\",\"/v1/users/ME/conversations/ALL/messages\",\"/v1/users/ME/contacts/ALL\",\"/v1/threads/ALL\"]}";
        packet.setData(id);
        packet.setType(RequestType.POST);
        packet.setUrl("https://" + url + "/v1/users/ME/endpoints/SELF/subscriptions");
        return packet.makeRequest() != null;
    }

    public String location(SkypeAPI api) throws FailedToLoginException {
        packet = new PacketBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints");
        packet.setType(RequestType.POST);
        packet.setSendLoginHeaders(false);

        packet.addHeader(new Header("Authentication", "skypetoken=" + api.getLoginTokens().getXToken()));
        packet.setData("{}");
        String data = packet.makeRequest();
        if (data == null) {
            throw new FailedToLoginException("Bad account!");
        }
        return packet.getCon().getHeaderField("Location");
    }
}
