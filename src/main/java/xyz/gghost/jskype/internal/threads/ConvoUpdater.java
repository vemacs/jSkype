package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.APILoadedEvent;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;


public class ConvoUpdater extends Thread{
    private final SkypeAPI api;
    private boolean first = true;

    public ConvoUpdater(SkypeAPI api) {
        this.api = api;
    }

    @Override
    public  void run() {
        while (this.isAlive()) {
            try {
                api.getSkypeInternals().getRequests().getStartupRequests().setupRecent();
                if (!api.isLoaded())
                    api.getEventManager().executeEvent(new APILoadedEvent());
                api.setLoaded(true);
            } catch (AccountUnusableForRecentException e) {
                if (first)
                    stop();
            } catch (Exception e){
                if (api.isDebugMode())
                    e.printStackTrace();
            }
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ignored) {}
            first = false;
        }
    }
}