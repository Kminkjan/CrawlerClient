package crawlingmodule;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import message.Message;
import message.MessageDone;
import message.MessageProcess;
import message.MessageUrl;
import util.URLData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A Module regulates the crawling and analysing of html pages. It is build so that
 * Modules can be added and removed in a modular fashion. The Module acts as a central hub
 * and data structure where the {@link Crawler} and {@link crawlingmodule.DataProcessor}
 * retrieve and store their data.
 * Created by KrisMinkjan on 14-2-2015.
 */
public class Module extends UntypedActor {

    /**
     * This Module's id
     */
    private final int id;

    /**
     * The Module's child components.
     */
    private final ActorRef crawler, processor, admin;
    private boolean crawlerNotified, processorNotified, waitForUrl, power;
    private final static int POLITE_DELAY = 500;

    /**
     * Object that where info about this Module is stored. Used for the GUI.
     */
    private final ModuleInfo info;

    /**
     * Time in milliseconds since the last received result from the {@link crawlingmodule.DataProcessor}.
     */
    private long previousStartTime = System.nanoTime();

    /**
     * Urls that are queued to be visited by the {@link Crawler}.
     */
    private final LinkedList<String> urlList;
    private final HashMap<String, Long> domainMap = new HashMap<String, Long>();

    /**
     * Creates a {@link Module}
     *
     * @param crawler   This Module's {@link crawlingmodule.Crawler}.
     * @param processor This Module's {@link DataProcessor}.
     * @param info      The info object of this module.
     */
    public Module(ActorRef crawler, ActorRef processor, ModuleInfo info, ActorRef admin, int id) {
        this.crawler = crawler;
        this.processor = processor;
        this.admin = admin;
        this.info = info;
        this.urlList = new LinkedList<String>();
        this.id = id;
    }

    @Override
    public void preStart() throws Exception {
        /* Notify the child Actors to start */
        crawler.tell(new Message(Message.MessageType.MODULE_NOTIFY), getSelf());
        processor.tell(new Message(Message.MessageType.MODULE_NOTIFY), getSelf());
    }

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case CRAWLER_NOTIFY:
                if (processorNotified) {
                    info.setStatus("running");
                }
                crawlerNotified = true;

                getSender().tell(new MessageUrl("http://jsoup.org"), getSelf());
                info.setCurrentUrl("http://jsoup.org");
                break;
            case PROCESSOR_NOTIFY:
                if (crawlerNotified) {
                    info.setStatus("running");
                }
                processorNotified = true;
                break;
            case REQUEST_URL:
                if (!urlList.isEmpty()) {
                    getSender().tell(new MessageUrl(selectNextUrl()), getSelf());
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
                processUrls(m.getUrlList(), m.getUrlData());
                break;
            case EDIT_VALUE:
                crawler.tell(message, getSelf());
                break;
        }
    }

    /**
     * Puts all the recieved urls in {@link #urlList}. Notifies the {@link Crawler} if
     * {@link #waitForUrl} is true.
     *
     * @param urls The urls that will be added.
     */
    private void processUrls(List<String> urls, URLData urlData) {
        urlList.addAll(urls);

        /* Check if the crawler should be notified */
        if (waitForUrl && !urlList.isEmpty()) {
            waitForUrl = false;
            String url = urlList.poll();
            crawler.tell(new MessageUrl(url), getSelf());
            info.setCurrentUrl(url);
        }

        admin.tell(new MessageDone(this.id, System.nanoTime() - previousStartTime, urlData), getSelf());

        previousStartTime = System.nanoTime(); // currentTimeMillis();
    }

    /**
     * Select which url to crawl next while taking a polite delay into account, which means that one domain doesn't get
     * more then 1 request per 2 seconds.
     *
     * @return The next url.
     */
    private String selectNextUrl() {
        String url = "";
        url = urlList.poll();
        if (!power && urlList.size() > 40) {
            try {
                /* currentTime = good enough */
                long currentTime = System.currentTimeMillis();
                String domain = getDomain(url);

                /* Search for an url that can be crawled */
                while ((domainMap.containsKey(domain) && POLITE_DELAY > (currentTime - domainMap.get(domain)))) {
                    urlList.offer(url);
                    url = urlList.poll();
                    domain = getDomain(url);
                }
                domainMap.put(domain, currentTime);
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
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
