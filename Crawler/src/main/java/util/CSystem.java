package util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;
import message.Message;
import message.MessageEditValue;
import message.MessageServer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kris on 11-4-2015.
 */
public class CSystem {
    private final ActorSystem system;
    private int threadCount, idCounter, urlsProcessed;
    private final LinkedList<ActorRef> modulelist;
   // TODO private final List<XYChart.Data> dataList;
    private HashMap<Integer, ModuleInfo> infoHashMap = new HashMap<Integer, ModuleInfo>();
    private final ActorRef admin;

    // TODO private SimpleStringProperty urlmin, urltotal;

    private ServerConnector serverConnector;


    /**
     * Creates the System.
     */
    public CSystem(List<XYChart.Data> dataList, SimpleStringProperty urlminProperty, SimpleStringProperty urltotalProperty,
                         SimpleStringProperty connectionProperty) {

        this.system = ActorSystem.create("crawler");
        this.modulelist = new LinkedList<ActorRef>();

        this.admin = system.actorOf(Props.create(Admin.class, this, connectionProperty));
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
        ActorRef module = system.actorOf(Props.create(Module.class, crawler, processor, tempInfo, admin, idCounter));

        modulelist.push(module);
        infoHashMap.put(idCounter++, tempInfo);

        serverConnector.tellServer("addthread");

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
        System.out.println("shutdown");
        system.stop(admin);
        system.shutdown();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Updates the info about a {@link crawlingmodule.Module}.
     *
     * @param module The id of the Module of which the {@link util.ModuleInfo} will be updated.
     * @param ms     Amount of milliseconds the previous request took.
     */
    public void updateInfo(int module, long ms) {
        infoHashMap.get(module).setPerformance(ms);
        ++urlsProcessed;
    }

//    /**
//     * Update the UI, is called async by the {@link system.Admin}.
//     */
//    public void update() {
//        int sum = 0;
//        for (ModuleInfo info : infoHashMap.values()) {
//            sum += Integer.parseInt(info.getPerformance());
//        }
//        urlmin.set("" + sum);
//        urltotal.set("" + urlsProcessed);
//        Calendar cal = Calendar.getInstance();
//        cal.getTime();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//
//        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), sum));
////        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), urlsProcessed));
//        if (dataList.size() > 20) {
//            dataList.remove(0);
//        }
//    }

    public void setServerConnector(ServerConnector connector) {
        this.serverConnector = connector;
    }
}
