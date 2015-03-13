package nl.minkjan.crawler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import android.app.Activity;
import android.widget.TextView;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import util.ModuleInfo;

/**
 * The system.CrawlerSystem contains the whole system of crawlers and acts as an interface to the {@link crawlingmodule.Module Crawler Modules}.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class CrawlerSystem {
    private final ActorSystem system;
    private final ActorRef admin;

    /**
     * Creates the System.
     */
    public CrawlerSystem(int amount, Activity activity) {
        this.system = ActorSystem.create("crawler");

        this.admin = system.actorOf(Props.create(Admin.class, activity));
        for (int i = 0; i < amount; i++) {
            addModule();
            System.out.println("\tthread created");
        }
        System.out.println("started crawling...");
    }

    /**
     * Add a {@link crawlingmodule.Module} to the system.
     *
     * @return The info about the m
     */
    public ModuleInfo addModule() {
        ModuleInfo tempInfo = new ModuleInfo("", "starting", "http://jsoup.org", "0");
        ActorRef processor = system.actorOf(Props.create(DataProcessor.class));
        ActorRef crawler = system.actorOf(Props.create(Crawler.class, processor, false));
        ActorRef module = system.actorOf(Props.create(Module.class, crawler, processor, tempInfo, admin, 0));
        return tempInfo;
    }

    /**
     * Shuts down the system
     */
    public void shutDown() {
        system.shutdown();
    }
}
