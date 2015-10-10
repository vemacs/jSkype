package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.SkypeAPI;

public class ContactUpdater extends Thread {

    private final SkypeAPI api;

    public ContactUpdater(SkypeAPI api) {
        this.api = api;
    }
    @Override
    public void run() {
        while (this.isAlive()) {
            try {
                api.getSkypeInternals().getRequests().getStartupRequests().setupContactsRealTime();
                Thread.sleep(7000);
            } catch (InterruptedException ignored) {} catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}