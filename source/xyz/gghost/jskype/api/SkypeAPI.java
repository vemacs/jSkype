package xyz.gghost.jskype.api;

import lombok.Getter;
import lombok.Setter;

import xyz.gghost.jskype.api.event.EventManager;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.BadUsernamePassword;
import xyz.gghost.jskype.exception.FailedToGetProfileException;
import xyz.gghost.jskype.exception.FailedToLoginException;
import xyz.gghost.jskype.internal.packet.packets.GetProfilePacket;
import xyz.gghost.jskype.internal.threading.*;
import xyz.gghost.jskype.internal.impl.Group;
import xyz.gghost.jskype.var.User;

import java.util.ArrayList;
import java.util.UUID;

public class SkypeAPI {

    public String cookies = "";
    @Getter UUID uuid = UUID.randomUUID();
    @Getter public Skype skype;
    private ArrayList<Group> knownGroups = new ArrayList<Group>();
    @Getter private EventManager eventManager = new EventManager();
    private Thread pinger;
    @Getter private Poller poller;
    private Thread contactUpdater;
    private ConvoUpdater convoUpdater;
    private PendingContactEventThread contactThread;
    @Setter @Getter boolean stfuMode = false;
    /** debug mode*/
    @Setter @Getter boolean debugMode = false;
    /** This will allow basic error messages to show but nothing related to debugging*/
    @Setter boolean basicLogging = true;

    public boolean displayInfoMessages(){
        return debugMode || basicLogging;
    }

    public SkypeAPI(String user, String pass, boolean multithread) throws Exception{
        this.skype = new Skype(user, pass, this);
        if (multithread) {
            new Thread() {
                @Override
                public void run(){
                    init();
                }
            }.start();
        }else{
            init();
        }
    }
    public SkypeAPI(String user, String pass) throws Exception{
        this.skype = new Skype(user, pass, this);
        init();
    }


    private void init() {
        //ORDER IS FOR A REASON
        pinger = new Ping(this);
        pinger.start();
        contactUpdater = new ContactUpdater(this.skype, this);
        contactUpdater.start();
        contactThread = new PendingContactEventThread(this.skype, this);
        contactThread.start();
        poller = new Poller(this, skype);
        poller.start();
        convoUpdater = new ConvoUpdater(this.skype, this);
        convoUpdater.start();

    }

    /**
     * Get User object from skype name
     */
    public User getUserFromName(String username) throws FailedToGetProfileException {
        User user = skype.getContact(username);
        return user != null ? user : new GetProfilePacket(this, skype).getUser(username);
    }

    /**
     * Stop threads related to this skype app.
     * If you don't have some kinda loop, the app will close.
     */
    public void stop() {
        pinger.stop();
        poller.stop();
        contactUpdater.stop();
        convoUpdater.stop();
        contactThread.stop();
    }


}
