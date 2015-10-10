package xyz.gghost.jskype.internal.packet.requests;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;

/**
 * Created by Ghost on 10/10/2015.
 */
public class Requests {
    @Getter
    private UserRankingRequest userRankingRequest;
    @Getter
    private GroupMetaRequest groupMetaRequest;
    @Getter
    private StartupRequests startupRequests;
    @Getter
    private SendMessage sendMessageRequest;
    @Getter
    private PingPrepRequest pingPrepRequest;
    @Getter
    private UserMetaRequest userMetaRequest;


    public Requests(SkypeAPI api){
        userRankingRequest = new UserRankingRequest(api);
        startupRequests = new StartupRequests(api);
        groupMetaRequest = new GroupMetaRequest(api);
        sendMessageRequest = new SendMessage(api);
        pingPrepRequest = new PingPrepRequest(api);
        userMetaRequest = new UserMetaRequest(api);
    }

}
