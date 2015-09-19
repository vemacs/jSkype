package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserPendingContactRequestEvent;
import xyz.gghost.jskype.internal.user.User;

import java.util.ArrayList;

/**
 * Created by Ghost on 19/09/2015.
 */
public class PendingContactEventThread extends Thread {

    private final SkypeAPI api;
    private boolean firstTime = true;
    private ArrayList<String> lastUsers = new ArrayList<String>();

    public PendingContactEventThread(SkypeAPI api) {
        this.api = api;
    }
    @Override
    public void run() {
        while(this.isAlive()){
            try {
                ArrayList<User> newRequests = api.getContactRequests();
                if (!firstTime) {
                    ArrayList<String> newLastUsers = new ArrayList<String>(); //allows other clients to accept the request
                    for (User user : newRequests) {
                        if(!lastUsers.contains(user.getUsername())){
                            api.getEventManager().executeEvent(new UserPendingContactRequestEvent(user.getUsername()));
                        }
                        newLastUsers.add(user.getUsername());
                    }
                    lastUsers = newLastUsers;
                }else{
                    for (User user : newRequests)
                        lastUsers.add(user.getUsername());
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ignored) {}
            firstTime = false;
        }
    }
}
