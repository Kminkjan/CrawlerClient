package system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawlingmodule.Crawler;
import crawlingmodule.DataProcessor;
import crawlingmodule.Module;
import message.*;
import util.DatabaseConnector;
import util.ModuleInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The system.CrawlerSystem contains the whole system of crawlers and acts as an interface to the {@link crawlingmodule.Module Crawler Modules}.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class CrawlerSystem {
    private final ActorSystem system;
    private int threadCount, idCounter, urlsProcessed;
    private final LinkedList<ActorRef> modulelist;
    private final List<XYChart.Data> dataList;
    private HashMap<Integer, ModuleInfo> infoHashMap = new HashMap<Integer, ModuleInfo>();
    private final ActorRef admin;

    private SimpleStringProperty urlmin, urltotal;


    /**
     * Creates the System.
     */
    public CrawlerSystem(List<XYChart.Data> dataList, SimpleStringProperty urlminProperty, SimpleStringProperty urltotalProperty) {
        this.system = ActorSystem.create("crawler");
        this.modulelist = new LinkedList<ActorRef>();

        this.admin = system.actorOf(Props.create(Admin.class, this));

        this.dataList = dataList;

        this.urlmin = urlminProperty;
        this.urltotal = urltotalProperty;

    }

    /**
     * Add a {@link crawlingmodule.Module} to the system.
     *
     * @return The info about the m
     */
    public ModuleInfo addModule() {
        ModuleInfo tempInfo = new ModuleInfo("thread " + threadCount++, "starting", "http://jsoup.org", "0");
        ActorRef processor = system.actorOf(Props.create(DataProcessor.class));
        ActorRef crawler = system.actorOf(Props.create(Crawler.class, processor, true));
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
     * @param module The id of the Module of which the {@link util.ModuleInfo} will be updated.
     * @param ms     Amount of milliseconds the previous request took.
     */
    public void updateInfo(int module, long ms) {
        infoHashMap.get(module).setPerformance(ms);
        ++urlsProcessed;
    }

    /**
     * Update the UI, is called async by the {@link system.Admin}.
     */
    public void update() {
        int sum = 0;
        for (ModuleInfo info : infoHashMap.values()) {
            sum += Integer.parseInt(info.getPerformance());
        }
        urlmin.set("" + sum);
        urltotal.set("" + urlsProcessed);
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), sum));
//        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), urlsProcessed));
        if (dataList.size() > 20) {
            dataList.remove(0);
        }
    }

    /**
     * Tell the admin to crawl active
     * @param startUrl
     */
    public void activate(String startUrl) {
        admin.tell(new MessageServer(startUrl, false), null);
    }

    /**
     * Tell the admin to refresh the server.
     */
    public void refresh() {
        admin.tell(new MessageServer("", true), null);
    }
}
