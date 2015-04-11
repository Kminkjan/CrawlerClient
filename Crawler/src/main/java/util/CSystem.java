package util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import message.Message;

import java.util.LinkedList;

/**
 * Created by Kris on 11-4-2015.
 */
public class CSystem {
    private final ActorSystem system;
    private int threadCount, idCounter;
    private final LinkedList<ActorRef> moduleList;
    private ActorRef admin;
    private final ServerConnector serverConnector;


    /**
     * Creates the System.
     */
    public CSystem(UICallables uiCallables) {
        this.system = ActorSystem.create("crawler");
        this.moduleList = new LinkedList<>();
        this.serverConnector = new ServerConnector(admin, uiCallables);
        this.admin = system.actorOf(Props.create(CAdmin.class, this, uiCallables, serverConnector));
    }

    /**
     * Add a {@link crawlingmodule.Module} to the system.
     *
     * @return The info about the m
     */
    public ModuleInfo addModule() {
        ModuleInfo tempInfo = new ModuleInfo("thread " + threadCount++, "starting", "pending", "0");
        ActorRef processor = system.actorOf(Props.create(DataProcessor.class));
        ActorRef crawler = system.actorOf(Props.create(Crawler.class, processor, true));
        ActorRef module = system.actorOf(Props.create(Module.class, crawler, processor, tempInfo, admin, idCounter++));

        moduleList.push(module);
        serverConnector.tellServer("addthread");

        return tempInfo;
    }

    /**
     * Removes {@link crawlingmodule.Module} and marks is for deletion.
     *
     * @return True if there is a {@link crawlingmodule.Module} available for deletion.
     */
    public boolean removeModule() {
        if (!moduleList.isEmpty()) {
            moduleList.pollFirst().tell(new Message(Message.MessageType.SHUT_DOWN), null);
            return true;
        }
        return false;
    }

    /**
     * Shuts down the system
     */
    public void shutDown() {
        System.out.println("shutdown");
        system.stop(admin);
        system.shutdown();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
