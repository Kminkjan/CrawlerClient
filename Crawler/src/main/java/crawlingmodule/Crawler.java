package crawlingmodule;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import message.Message;
import message.MessageDocument;
import message.MessageEditValue;
import message.MessageUrl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The task of the Crawler is fetching HTML pages from urls and sending it to the {@link crawlingmodule.DataProcessor}.
 * The lifecycle of the Crawler starts by sending a {@link message.Message.MessageType REQUEST_URL} {@link message.Message} to the
 * {@link crawlingmodule.Module} to request an url. When an url is received, it will crawl the web with {@link #doCrawl(String)}.
 * When done, the Crawler will send the resulting HTML {@link org.jsoup.nodes.Document} to the {@link crawlingmodule.DataProcessor}.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class Crawler extends UntypedActor {
    /**
     * This Crawler's associated {@link crawlingmodule.DataProcessor}.
     */
    private ActorRef processor;

    /**
     * The crawl delay is the time between consequent crawl request. The Crawler will space at least this amount of time
     * between requests.
     */
    private int CRAWL_DELAY = 150;
    private final boolean verbose;

    /**
     * Create a Crawler
     *
     * @param processor This Crawler's associated {@link crawlingmodule.DataProcessor}.
     */
    public Crawler(ActorRef processor, boolean verbose) {
        this.processor = processor;
        this.verbose = verbose;
    }


    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case MODULE_NOTIFY:
                getSender().tell(new Message(Message.MessageType.CRAWLER_NOTIFY), getSelf());
                break;
            case GIVE_URL:
                long startTime = System.nanoTime();

                doCrawl(((MessageUrl) message).getUrlData(), ((MessageUrl) message).getDepth());

                getSender().tell(new Message(Message.MessageType.REQUEST_URL), getSelf());

//                try {
//                    checkRobot(getDomain(((MessageUrl) message).getUrlData()));
//                } catch (IOException e) {
//                    System.out.println(e.getLocalizedMessage());
//                }

                long sleepTime = CRAWL_DELAY - TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                break;
            case EDIT_VALUE:
                MessageEditValue m2 = (MessageEditValue) message;
                switch (m2.getValueType()) {
                    case DELAY:
                        this.CRAWL_DELAY = m2.getValue();
                        break;
                }
                break;
        }
    }

    /**
     * Crawls the given url and sends the result to {@link #processor}.
     *
     * @param urlData The url to be crawled.
     */
    private void doCrawl(String urlData, int depth) {
        try {
            Document doc = Jsoup.connect(urlData).timeout(1000).get();
            processor.tell(new MessageDocument(doc, depth), getSelf());
        } catch (IOException e) {
            if (verbose) {
                System.out.println("error: " + e.getLocalizedMessage() + "\n url: " + urlData);
            }
        }
    }

    /**
     * TODO check te robot.txt file
     * @param domain
     * @throws java.io.IOException
     */
    private void checkRobot(String domain) throws IOException {
//        URL robotURL = new URL("http://" + domain + "/robots.txt");
//        BufferedReader in = new BufferedReader(new InputStreamReader(robotURL.openStream()));
//        String line = null;
//        while((line = in.readLine()) != null) {
//            System.out.println(line);
//        }
    }


}

