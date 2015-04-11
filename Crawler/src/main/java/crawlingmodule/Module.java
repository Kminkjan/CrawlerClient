package crawlingmodule;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import message.*;
import util.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * A Module regulates the crawling and analysing of html pages. It is build so that
 * Modules can be added and removed in a modular fashion. The Module acts as a central hub
 * and data structure where the {@link Crawler} and {@link crawlingmodule.DataProcessor}
 * retrieve and store their data.
 * Created by KrisMinkjan on 14-2-2015.
 */
public class Module extends UntypedActor {

    private final static Logger LOGGER = Logger.getLogger(Module.class.getName());

    /**
     * This Module's id
     */
    private final int id;

    private int currentSearchId;
    private String currentTag = "java";

    /**
     * The Module's child components.
     */
    private final ActorRef crawler, processor, admin;
    private boolean crawlerNotified, processorNotified, waitForUrl, power, onOrder, active = false;
    private final static long POLITE_DELAY = TimeUnit.NANOSECONDS.convert(700, TimeUnit.MILLISECONDS);

    /**
     * Object that where info about this Module is stored. Used for the GUI.
     */
    private final SimpleModuleInfo info;

    /**
     * Time in milliseconds since the last received result from the {@link crawlingmodule.DataProcessor}.
     */
    private long previousStartTime = System.nanoTime();

    /**
     * Urls that are queued to be visited by the {@link Crawler}.
     */
    private final LinkedList<String> urlList;
    private final HashMap<String, Long> domainMap = new HashMap<String, Long>();

    private final PriorityQueue<DepthData> activeUrlQueue = new PriorityQueue<DepthData>();
    private int maxDepth = 0;

    /**
     * Creates a {@link Module}
     *
     * @param crawler   This Module's {@link crawlingmodule.Crawler}.
     * @param processor This Module's {@link DataProcessor}.
     * @param info      The info object of this module.
     */
    public Module(ActorRef crawler, ActorRef processor, SimpleModuleInfo info, ActorRef admin, int id) {
        this.crawler = crawler;
        this.processor = processor;
        this.admin = admin;
        this.info = info;
        this.urlList = new LinkedList<String>();
        this.id = id;
    }

    @Override
    public void preStart() throws Exception {
        LOGGER.info("Module started");
        /* Notify the child Actors to start */
        crawler.tell(new Message(Message.MessageType.MODULE_NOTIFY), getSelf());
        processor.tell(new Message(Message.MessageType.MODULE_NOTIFY), getSelf());
//        activeUrlQueue.add(new DepthData("http://jsoup.org", 1));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case CRAWLER_NOTIFY:
                if (processorNotified) {
                    info.setStatus("idle");
                }
                crawlerNotified = true;

                DatabaseConnector dbc = new DatabaseConnector();

                urlList.addAll(dbc.getRandomUrl());

                getSender().tell(new MessageUrl(selectNextUrl(), 0), getSelf());
                //getSender().tell(new MessageUrl("https://java.com/nl/download/", 0), getSelf());
                //info.setCurrentUrl("https://java.com/nl/download/");
                break;
            case PROCESSOR_NOTIFY:
                if (crawlerNotified) {
                    info.setStatus("idle");
                }
                processorNotified = true;
                break;
            case REQUEST_URL:
                if (active && !activeUrlQueue.isEmpty()) {
                    DepthData data = activeUrlQueue.poll();
                    info.setCurrentUrl(data.getUrl());
                    getSender().tell(new MessageUrl(data.getUrl(), data.getDepth()), getSelf());
                } else if (!active && !urlList.isEmpty()) {
                    getSender().tell(new MessageUrl(selectNextUrl(), 0), getSelf());
                } else {
                    waitForUrl = true;
                }
                break;
            case SHUT_DOWN:
                info.setStatus("shutting down");
                Thread.sleep(2000);
                info.setStatus("stopped");
                break;
            case PROCESSED_URLS:
                MessageProcess m = (MessageProcess) message;
                processUrls(m.getUrlList(), m.getUrlData(), m.getDepth());
                break;
            case EDIT_VALUE:
                crawler.tell(message, getSelf());
                break;
            case ORDER:
                urlList.clear();
                urlList.addAll(((MessageOrder) message).getOrderList());
                onOrder = true;
                info.setStatus("refresh database");
                break;
            case ACTIVE:
                MessageActive ma = (MessageActive) message;
                this.active = true;
                String starturl = ma.getStartUrl();
                if (starturl.contains("google")) {
                    LOGGER.warning("google detected");
                    Googler g = new Googler();
                    for(String s : g.getDataFromGoogle(starturl)) {
                        activeUrlQueue.offer(new DepthData(s, 1));
                        System.out.println(s);
                    }
                } else {
                    activeUrlQueue.offer(new DepthData(starturl, 1));
                }
                this.maxDepth = ma.getMaxDepth();
                this.currentSearchId = ma.getSearchId();
                this.currentTag = ma.getTag();
                info.setStatus("active");
                break;
            case ACTIVE_PROCESS:
//                System.out.println("recieved");
                MessageActiveDone mad = (MessageActiveDone) message;
                processActive(mad.getActiveURLData());
                break;
        }
    }

    private void processActive(ActiveURLData activeURLData) {
        LOGGER.info("Module: ProcessActive");

        if (activeURLData.getDepth() <= maxDepth) {
            for (String link : activeURLData.getLinkList()) {
                activeUrlQueue.add(new DepthData(link, activeURLData.getDepth() + 1));
//                System.out.println("added: " + link);
            }
            if (waitForUrl && !activeUrlQueue.isEmpty()) {
                waitForUrl = false;
                DepthData url = activeUrlQueue.poll();
                crawler.tell(new MessageUrl(url.getUrl(), url.getDepth()), getSelf());
                info.setCurrentUrl(url.getUrl());
            }
            System.out.println(activeURLData.getUrl() + " | depth: " + activeURLData.getDepth());

            activeURLData.setSearchId(currentSearchId);
            activeURLData.setTag(currentTag);

            admin.tell(new MessageDoneActive(this.id, System.nanoTime() - previousStartTime, activeURLData), getSelf());

            System.out.println("Active time: " + TimeUnit.MILLISECONDS.convert((System.nanoTime() - previousStartTime), TimeUnit.NANOSECONDS));
            previousStartTime = System.nanoTime();
        } else {
            System.out.println("DONE, continueing idle crawling");
            admin.tell(new Message(Message.MessageType.MODULE_NOTIFY), getSelf());
            active = false;
            info.setStatus("idle");
            // urlList.addAll(activeURLData.getLinkList().subList(0, 20));
            if (waitForUrl) {
                /* Continue idle crawling */
                crawler.tell(new MessageUrl(activeURLData.getUrl(), 0), getSelf());
            }
        }
    }

    /**
     * Puts all the recieved urls in {@link #urlList}. Notifies the {@link Crawler} if
     * {@link #waitForUrl} is true.
     *
     * @param urls The urls that will be added.
     */
    private void processUrls(List<String> urls, List<URLData> urlData, int depth) {
        LOGGER.info("MODULE: amount = " + urlData.size());

        if (onOrder && urlList.isEmpty()) { // TODO this is funky
            onOrder = false;
            info.setStatus("idle");
            urlList.addAll(urls);
        } else if (urlList.size() < 256) {
            urlList.addAll(urls);
        }

        /* Check if the crawler should be notified */
        if (waitForUrl && !urlList.isEmpty()) {
            waitForUrl = false;
            String url = urlList.poll();
            crawler.tell(new MessageUrl(url, 0), getSelf());
            info.setCurrentUrl(url);
        }

        admin.tell(new MessageDone(this.id, System.nanoTime() - previousStartTime, urlData, !onOrder), getSelf());

        previousStartTime = System.nanoTime();
    }

    /**
     * Select which url to crawl next while taking a polite delay into account, which means that one domain doesn't get
     * more then 1 request per 2 seconds.
     *
     * @return The next url.
     */
    private String selectNextUrl() {
        LOGGER.info("Module: urlList.size = " + urlList.size() + "domainMap.size = " + domainMap.size());

        String url = urlList.poll();
        if (!power && urlList.size() > 40) {
            try {
                /* currentTime = good enough */
                long currentTime = System.nanoTime();
                String domain = getDomain(url);

                int times = 0;

                /* Search for an url that can be crawled */
                while ((domainMap.containsKey(domain) && POLITE_DELAY > (currentTime - domainMap.get(domain)))) {
                    urlList.offer(url);
                    url = urlList.poll();
                    domain = getDomain(url);
                    // System.out.print("|");
                    if(++times % 40 == 0) {
                        currentTime = System.nanoTime();
                        LOGGER.warning("Url selection takes a long time! Times:  " + times + " used time: " + currentTime + " url: " + url);
                        if (times > 119) {
                            LOGGER.severe("System stalled and is reset");
                            //System.exit(1);

                            urlList.clear();
                            DatabaseConnector dbc = new DatabaseConnector();
                            urlList.addAll(dbc.getRandomUrl());
                        }
                    }
                }
                domainMap.put(domain, currentTime);
                if (domainMap.size() > 100) {
                    domainMap.clear();
                }
            } catch (URISyntaxException e) {
                System.out.println("URL: " + url);
                e.printStackTrace();
            }
        }
        info.setCurrentUrl(url);
        return url;
    }

    /**
     * Retrieves the domain String from an url.
     *
     * @param url The url to be processed.
     * @return This url's domain.
     * @throws java.net.URISyntaxException
     */
    private String getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            System.out.println("null url: " + url);
            return null;
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
