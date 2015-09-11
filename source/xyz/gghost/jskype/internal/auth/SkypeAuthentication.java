package xyz.gghost.jskype.internal.auth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.BadUsernamePassword;
import xyz.gghost.jskype.exception.FailedToLoginException;
import xyz.gghost.jskype.exception.RecaptchaException;
import xyz.gghost.jskype.internal.packet.Header;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
public class SkypeAuthentication {

    private String url = "";
    private PacketBuilder packet;
    private String tSkypeToken;
    private String cookies;

    private Document postData(String username, String password, SkypeAPI api, Skype acc) throws BadResponseException {
        Date now = new Date();

        PacketBuilder getIdsPacket = new PacketBuilder(api);
        getIdsPacket.setSendLoginHeaders(false);
        getIdsPacket.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");
        getIdsPacket.setType(RequestType.GET);

        String htmlIds = getIdsPacket.makeRequest(acc);
        if (htmlIds == null)
            throw new BadResponseException();
        String pie = htmlIds.split("name=\"pie\" id=\"pie\" value=\"")[1].split("\"")[0];
        String etm = htmlIds.split("name=\"etm\" id=\"etm\" value=\"")[1].split("\"")[0];

        StringBuilder data = new StringBuilder();
        data.append("username=" + URLEncoder.encode(username));
        data.append("&password=" + URLEncoder.encode(password));
        data.append("&timezone_field=" + URLEncoder.encode(new SimpleDateFormat("XXX").format(now).replace(':', '|')));
        data.append("&js_time=" + String.valueOf(now.getTime() / 1000));
        data.append("&pie=" + URLEncoder.encode(pie));
        data.append("&etm=" + URLEncoder.encode(etm));
        data.append("&client_id=" + URLEncoder.encode("578134"));
        data.append("&redirect_uri=" + URLEncoder.encode("https://web.skype.com"));

        String formData = data.toString();

        PacketBuilder login = new PacketBuilder(api);
        login.setSendLoginHeaders(false);
        login.setType(RequestType.POST);
        login.setIsForm(true);
        login.setData(formData);
        login.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");

        String html = login.makeRequest(acc);
        if (html == null)
            throw new BadResponseException();

        return Jsoup.parse(html);

    }
    public void login(SkypeAPI api, Skype account) throws Exception {
        Document loginResponse = postData(account.getUsername(), account.getPassword(), api, account);
        handle(loginResponse, account, api);
        try{
            prepare(api, account);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void handle(Document loginResponseDocument, Skype account, SkypeAPI api) throws Exception {
        Elements inputs = loginResponseDocument.select("input[name=skypetoken]");
        if (inputs.size() > 0) {
            tSkypeToken = inputs.get(0).attr("value");
            account.getLoginTokens().setXToken(tSkypeToken);
        } else if (loginResponseDocument.html().contains("https://www.google.com/recaptcha/")) {
            System.out.println("Failed to connect due to a recaptcha!");

            throw new RecaptchaException();
            //TESTING SOME STUFF
            /*
            String id = loginResponseDocument.html().split("https://www.google.com/recaptcha/")[1].split("k=")[1].split("\"></script>")[0];


            System.out.println(loginResponseDocument.html());

            String chalId = "";

            PacketBuilder google = new PacketBuilder(api);
            google.setSendLoginHeaders(false);
            google.setUrl("https://www.google.com/recaptcha/api/challenge?k=" + id);
            google.setType(RequestType.GET);
            String data = google.makeRequest(account);
            if (data != null){
                chalId = data.split("challenge : '")[1].split("',")[0];
            }

            PacketBuilder renew  = new PacketBuilder(api);
            renew.setSendLoginHeaders(false);
            renew.setUrl("http://www.google.com/recaptcha/api/reload?c=" + chalId + "&k=6Lc9KQwAAAAAAK2Egvu8-_F_tR161wkdIlRslemS&reason=i&type=image&lang=en-GB");
            renew.setType(RequestType.GET);
            String renewData = renew.makeRequest(account);


            UserRecaptchaEvent event = new UserRecaptchaEvent(account.getEmail(), "https://www.google.com/recaptcha/api/image?c=" + chalId);

            api.getEventManager().executeEvent(event);

            Response attemptTwo = postData(account.getEmail(), account.getPassword(), chalId, event.getAnswer());
            handle(loginResponse, account, api);
            */

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
    }


    public void prepare(SkypeAPI api, Skype account) throws BadUsernamePassword, FailedToLoginException{
        authLogin(api, account);
        if (!reg(api, account)) {
            System.out.println("Failed to get update data from skype due to a login error... Attempting to relogin, however this wont work until the auto pinger kicks in.");
            authLogin(api, account);
            try {
                Thread.sleep(1750);
                prepare(api, account);
            } catch (InterruptedException e) {}
        }
        save(api, account);
    }


    public void authLogin(SkypeAPI api, Skype account) throws FailedToLoginException{
        url = location(api).split("://")[1].split("/")[0];
        account.getLoginTokens().setReg(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[0]);
        account.getLoginTokens().setEndPoint(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[2].split("=")[1]);
    }

    public boolean save(SkypeAPI api, Skype account) {
        String id = "{\"id\":\"messagingService\",\"type\":\"EndpointPresenceDoc\",\"selfLink\":\"uri\",\"publicInfo\":{\"capabilities\":\"video|audio\",\"type\":\"1\",\"skypeNameVersion\":\"skype.com\",\"nodeInfo\":\"2\",\"version\":\"2\"},\"privateInfo\":{\"epname\":\"Skype\"}}";
        PacketBuilder packet = new PacketBuilder(api);
        packet.setType(RequestType.PUT);
        packet.setData(id);
        packet.setUrl("https://" + url + "/v1/users/ME/endpoints/" + account.getLoginTokens().getEndPoint() + "/presenceDocs/messagingService");
        return packet.makeRequest(account) != null;
    }

    public boolean reg(SkypeAPI api, Skype account) {
        PacketBuilder packet = new PacketBuilder(api);
        String id = "{\"channelType\":\"httpLongPoll\",\"template\":\"raw\",\"interestedResources\":[\"/v1/users/ME/conversations/ALL/properties\",\"/v1/users/ME/conversations/ALL/messages\",\"/v1/users/ME/contacts/ALL\",\"/v1/threads/ALL\"]}";
        packet.setData(id);
        packet.setType(RequestType.POST);
        packet.setUrl("https://" + url + "/v1/users/ME/endpoints/SELF/subscriptions");
        return packet.makeRequest(account) != null;
    }

    public String location(SkypeAPI api)throws FailedToLoginException {
        packet = new PacketBuilder(api);
        packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints");
        packet.setType(RequestType.POST);
        packet.setSendLoginHeaders(false);
        packet.addHeader(new Header("Authentication", "skypetoken=" + tSkypeToken));
        packet.setData("{}");
        String data = packet.makeRequest(api.getSkype());
        if (data == null) {
            throw new FailedToLoginException("Bad account!");
        }
        return packet.getCon().getHeaderField("Location");
    }
}
