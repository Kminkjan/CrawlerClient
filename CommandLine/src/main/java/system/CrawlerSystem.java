package system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import message.Message;
import message.MessageEditValue;
import util.ModuleInfo;
import util.SimpleModuleInfo;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The system.CrawlerSystem contains the whole system of crawlers and acts as an interface to the {@link crawlingmodule.Module Crawler Modules}.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class CrawlerSystem {
    private final ActorSystem system;
    private final ActorRef admin;
    private final boolean verbose;

    /**
     * Creates the System.
     */
    public CrawlerSystem(int amount, boolean verbose) {
        this.system = ActorSystem.create("crawler");
        this.verbose = verbose;

        this.admin = system.actorOf(Props.create(Admin.class, verbose));
        for (int i = 0; i < amount; i++) {
            addModule();
            System.out.println("\tthread created");
        }
        System.out.println("started crawling...");
    }

    /**
     * Add a {@link crawlingmodule.Module} to the system.
     *
     */
    public void addModule() {
        // ModuleInfo tempInfo = new ModuleInfo("", "starting", "http://jsoup.org", "0");
        ActorRef processor = system.actorOf(Props.create(DataProcessor.class));
        ActorRef crawler = system.actorOf(Props.create(Crawler.class, processor, verbose));
        ActorRef module = system.actorOf(Props.create(Module.class, crawler, processor, new SimpleModuleInfo(), admin, 0));
    }

    /**
     * Shuts down the system
     */
    public void shutDown() {
        system.shutdown();
    }
}
