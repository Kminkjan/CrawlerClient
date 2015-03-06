package system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import crawlingmodule.ModuleInfo;
import message.Message;
import message.MessageEditValue;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The system.CrawlerSystem contains the whole system of crawlers and acts as an interface to the {@link crawlingmodule.Module Crawler Modules}.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class CrawlerSystem {
    private final ActorSystem system;
    private int threadCount, idCounter, urlsProcessed;
    private final LinkedList<ActorRef> modulelist;
    private HashMap<Integer, ModuleInfo> infoHashMap = new HashMap<Integer, ModuleInfo>();
    private final ActorRef admin;

    /**
     * Creates the System.
     */
    public CrawlerSystem(int amount) {
        this.system = ActorSystem.create("crawler");
        this.modulelist = new LinkedList<ActorRef>();

        this.admin = system.actorOf(Props.create(Admin.class));
        for (int i = 0; i < amount; i++) {
            addModule();
            System.out.println("\tThread created");
        }
    }

    /**
     * Add a {@link crawlingmodule.Module} to the system.
     *
     * @return The info about the m
     */
    public ModuleInfo addModule() {
        ModuleInfo tempInfo = new ModuleInfo("thread " + threadCount++, "starting", "http://jsoup.org", "0");
        ActorRef processor = system.actorOf(Props.create(DataProcessor.class));
        ActorRef crawler = system.actorOf(Props.create(Crawler.class, processor));
        ActorRef module = system.actorOf(Props.create(Module.class, crawler, processor, tempInfo, admin, idCounter));

        modulelist.push(module);
        infoHashMap.put(idCounter++, tempInfo);

        return tempInfo;
    }

    /**
     * Removes {@link crawlingmodule.Module} and marks is for deletion.
     *
     * @return True if there is a {@link crawlingmodule.Module} available for deletion.
     */
    public boolean removeModule() {
        if (!modulelist.isEmpty()) {
            modulelist.pollFirst().tell(new Message(Message.MessageType.SHUT_DOWN), null);
            return true;
        }
        return false;
    }

    /**
     * Shuts down the system
     */
    public void shutDown() {
        system.shutdown();
    }

    /**
     * Sets the delay between crawler requests.
     *
     * @param value The value the new delay should be.
     */
    public void setDelay(int value) {
        int delay = 100 + (100 - value); /*(int) (value < 50 ? 100 - value * 2 : 100 + (value - 50 * (1 + value / 100))); */
        System.out.println("Slider: " + delay);
        for (ActorRef module : modulelist) {
            module.tell(new MessageEditValue(MessageEditValue.ValueType.DELAY, delay), null);
        }
    }

    /**
     * Updates the info about a {@link crawlingmodule.Module}.
     *
     * @param module The id of the Module of which the {@link crawlingmodule.ModuleInfo} will be updated.
     * @param ms     Amount of milliseconds the previous request took.
     */
    public void updateInfo(int module, long ms) {
        infoHashMap.get(module).setPerformance(ms);
        ++urlsProcessed;
    }
}
