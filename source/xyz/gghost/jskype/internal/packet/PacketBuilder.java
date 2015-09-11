package xyz.gghost.jskype.internal.packet;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.api.Skype;
import xyz.gghost.jskype.api.SkypeAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class PacketBuilder {
    protected SkypeAPI api;
    //TODO: Recode -> this is from an older version of jSkype
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
    public PacketBuilder(SkypeAPI api) {
        this.api = api;
    }

    @Deprecated
    protected void addLogin(Skype usr) {

        addHeader(new Header("RegistrationToken", usr.getLoginTokens().getReg()));
        addHeader(new Header("X-Skypetoken", usr.getLoginTokens().getXToken()));
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public String makeRequest(Skype usr) {
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

            con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : (file ? "application/octet-stream" : "application/json; charset=utf-8"));
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("User-Agent", "0/7.7.0.103// libhttpX.X");
            if (!cookies.equals(""))
                con.setRequestProperty("Cookie", cookies);
            con.setDoOutput(true);
            if (sendLoginHeaders)
                addLogin(usr);
            for (Header s : headers) {
                con.addRequestProperty(s.getType(), s.getData());
            }
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
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString() == null ? "" : response.toString();

            } else if (code == 401) {
                System.out.println("\n\nBad login...");
                System.out.println(this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");
                return "---";
            } else if (code == 204) {
                return "";
            } else {
                if (code == 404 && url.toLowerCase().contains("endpoint")) {
                    System.out.println("Lost connection to skype.\nReloggin!");
                    try {
                        api.getSkype().relog();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if (api.isDebugMode()) {
                    if (!api.isStfuMode()) {
                        //GetProfile will handle the debugging info
                        if (url.equals("https://api.skype.com/users/self/contacts/profiles"))
                            return null;
                        //Debug info
                        System.out.println("Error contacting skype\nUrl: " + url + "\nCode: " + code + "\nData: " + data);
                        for (Header header : headers) {
                            System.out.println(header.getType() + ": " + header.getData());
                        }
                    }
                }
                return null;
            }
        } catch (UnknownHostException | SocketException e) {
            return null;
        } catch (Exception e) {
            if (api.displayInfoMessages()) {
                System.out.println("================================================");
                System.out.println("========Unable to request  the skype api========");
                System.out.println("================================================");
                System.out.println("Error: " + e.getMessage());
                System.out.println("URL: " + url);
            }
            e.printStackTrace();
            return null;
        }
    }

    /***
     * a simple request WILL NOT process any data received, you'll have to do that.
     *
     * @return
     */
    public HttpURLConnection simpleRequest(Skype usr) {
        try {

                URL obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

                con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : "application/json");
                con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
                //con.setRequestProperty("User-Agent", "JavaSkypeProject");
                con.setRequestProperty("Cookie", api.cookies);
                con.setDoOutput(true);
                addLogin(usr);
                for (Header s : headers) {
                    con.addRequestProperty(s.getType(), s.getData());
                }
                if (!(data.getBytes().length == 0)) {
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.write(data.getBytes());
                    wr.flush();
                    wr.close();
                }
                return con;
        } catch (Exception e) {
            System.out.println("================================================");
            System.out.println("========Unable to request  the skype api========");
            System.out.println("================================================");
            return null;
        }
    }

}
