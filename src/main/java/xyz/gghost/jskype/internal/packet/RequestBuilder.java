package xyz.gghost.jskype.internal.packet;

import xyz.gghost.jskype.SkypeAPI;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class RequestBuilder {
    protected SkypeAPI api;
    @Getter @Setter protected String data = "";
    @Getter @Setter protected String url = "";
    @Getter @Setter protected RequestType type = null;
    @Getter @Setter protected Boolean isForm = false;
    @Getter protected ArrayList<Header> headers = new ArrayList<Header>();
    @Getter protected HttpURLConnection con;
    @Getter @Setter protected boolean sendLoginHeaders = true;
    @Getter @Setter protected boolean file = false;
    @Getter @Setter protected int code = 200;
    @Getter @Setter protected String cookies = "";
    public RequestBuilder(SkypeAPI api) {
        this.api = api;
    }

    @Deprecated
    protected void addLogin(SkypeAPI skype) {
        addHeader(new Header("RegistrationToken", skype.getLoginTokens().getReg()));
        addHeader(new Header("X-Skypetoken", skype.getLoginTokens().getXToken()));
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public String makeRequest() {
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

            con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : (file ? "application/octet-stream" : "application/json; charset=utf-8"));
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("User-Agent", "0/7.7.0.103// libhttpX.X");
            con.setDoOutput(true);

            if (!cookies.equals(""))
                con.setRequestProperty("Cookie", cookies);

            if (sendLoginHeaders)
                addLogin(api);

            for (Header s : headers)
                con.addRequestProperty(s.getType(), s.getData());

            if (!(data.getBytes().length == 0)) {
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }

            code = con.getResponseCode();
            if (code == 200 || code == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);

                in.close();
                return response.toString() == null ? "" : response.toString();

            } else if (code == 401) {
                api.log("\n\nBad login...");
                api.log(this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");
                for (Header header : headers)
                    api.log(header.getType() + ": " + header.getData());
                return "---";
            } else if (code == 204) {
                return "";
            } else {

                if (code == 404 && url.toLowerCase().contains("endpoint")){
                    try {
                        api.login();
                    }catch(Exception e){}
                }

                //GetProfile will handle the debugging info
                if (url.equals("https://api.skype.com/users/self/contacts/profiles"))
                    return null;

                //Debug info
                if (api.isDebugMode()) {
                    for (StackTraceElement st : Thread.currentThread().getStackTrace())
                        System.out.println(st);

                    api.log("Error contacting skype\nUrl: " + url + "\nCode: " + code + "\nData: " + data + "\nType: " + type);
                    for (Header header : headers)
                        api.log(header.getType() + ": " + header.getData());
                }
                return null;
            }

        } catch (Exception e) {
            if (e.getMessage().contains("sun.security.validator.ValidatorException"))
                return null; //Java or microsoft just had a brain fart

            api.log("================================================");
            api.log("========Unable to request  the skype api========");
            api.log("================================================");
            api.log("Error: " + e.getMessage());
            api.log("URL: " + url);
            return null;
        }
    }
}
