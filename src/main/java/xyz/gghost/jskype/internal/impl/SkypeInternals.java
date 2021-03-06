package xyz.gghost.jskype.internal.impl;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.auth.Auth;
import xyz.gghost.jskype.internal.packet.requests.Requests;
import xyz.gghost.jskype.internal.threads.*;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.user.OnlineStatus;

import java.util.HashMap;

/**
 * Created by Ghost on 10/10/2015.
 */
public class SkypeInternals {
    private SkypeAPI api;

    protected Poller poller;
    protected Thread contactUpdater;
    protected Thread pinger;
    protected ConvoUpdater convoUpdater;
    protected PendingContactEventThread pendingContactThread;

    @Getter private Requests requests;
    @Getter private HashMap<String, MessageHistory> a = new HashMap<String, MessageHistory>();  //Could use an interface to hide this but its not worth it
    @Getter private boolean reloggin = false;

    public SkypeInternals(SkypeAPI api){
        this.api = api;
    }

    public void init() {
        //TODO: runnables
        requests = new Requests(api);
        pinger = new Ping(api);
        pinger.start();
        contactUpdater = new ContactUpdater(api);
        contactUpdater.start();
        pendingContactThread = new PendingContactEventThread(api);
        pendingContactThread.start();
        poller = new Poller(api);
        poller.start();
        convoUpdater = new ConvoUpdater(api);
        convoUpdater.start();
    }

    public void login() throws Exception{
        new Auth().login(api);          //init the authentication method and update the tokens
        reloggin = true;                //Don't allow relog spam
        init();                         //start threads
        api.updateStatus(OnlineStatus.ONLINE); //we're online!
    }

    public void stop(){
        poller.stopThreads();           //removed
        pinger.stop();
        contactUpdater.stop();
        poller.stop();
        convoUpdater.stop();
        pendingContactThread.stop();
    }
}
