package crawlingmodule;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import message.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.ActiveURLData;
import util.URLData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * The DataProcessor processes and analyses html {@link org.jsoup.nodes.Document Documents}. When it receives a
 * Document, the processor starts processing it by first subtracting urls form de Document, sending those to this
 * processor's {@link Module} and finally analysing the rest of the page. When this is done, the
 * processor will send it's results to the database.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class DataProcessor extends UntypedActor {

    private ActorRef module;

    /**
     * Documents are analysed here.
     */
    private final Analyser analyser = new Analyser();

    /**
     * Dont crawl any of these extensions
     */
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpe?g"
            + "|png|mp3|mp3|zip|gz|exe|jar))$");

    /**
     * If an url contatins these characters they will be excluded
     */
    private final static Pattern EXCLUDE = Pattern.compile("[$|%|#|@|?]");


    /**
     * HashSet with all the already visited urls, so that an url is not visited twice
     * <p/>
     */
    private final HashSet<String> visitedUrls = new HashSet<String>(), switchBuffer = new HashSet<String>();

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case MODULE_NOTIFY:
                module = getSender();
                module.tell(new Message(Message.MessageType.PROCESSOR_NOTIFY), getSelf());
                break;
            case DOCUMENT:
                MessageDocument md = (MessageDocument) message;
                processDocument(md.getDoc(), md.getDepth());
                break;
        }
    }

    /**
     * processes the document.
     *
     * @param doc The document to process
     */
    private void processDocument(Document doc, int depth) {
//        System.out.println("process: " + doc.location() + " " + depth);
        if (doc == null) {
            System.out.println("document is null");
        } else {
            List<URLData> urlData;
            if (doc.location().length() >= 255) {
                module.tell(new MessageProcess(selectUrls(doc.select("a"), 5), new ArrayList<URLData>(), depth), getSelf());
            }
            else if (depth == 0) {
//                System.out.println("idle data proc");
                urlData = analyser.analyseDocument(doc);
                if (urlData != null) {
                    module.tell(new MessageProcess(selectUrls(doc.select("a"), 5), urlData, depth), getSelf());
                }
            } else {
                Elements e = doc.select("a");
                ActiveURLData activeURLData = new ActiveURLData(doc.location(), null, 1, depth, selectUrls(e, e.size()));
                module.tell(new MessageActiveDone(activeURLData), getSelf());
                // TODO urlData = analyser.analyseActive(doc); rate the page by url
            }

        }
    }


    /**
     * Select some urls from the document to send to the {@link Module}.
     *
     * @param elements The elements to choose from.
     */
    private List<String> selectUrls(Elements elements, int amount) {
        List<String> tempList = new ArrayList<String>();
        Random random = new Random();
        int elementCount = elements.size();

        if (elementCount > 0) {
            /* Try to select 5 urls */
            for (int i = 0; i < amount; i++) {
                String potentialUrl = elements.get(random.nextInt(elementCount)).attr("abs:href");
                if (potentialUrl != null && legitUrl(potentialUrl)) {

                    /* Check if the buffer is filled */
                    if (visitedUrls.size() > 9900) {
                        /* fill buffer */
                        if (visitedUrls.size() > 10000) {
                            /* Reset the visited hashmap and fill with the buffer */
                            visitedUrls.clear();
                            visitedUrls.addAll(switchBuffer);
                            switchBuffer.clear();
                            System.out.println("Buffer reset");
                        } else {
                            switchBuffer.add(potentialUrl);
                        }
                    }
                    visitedUrls.add(potentialUrl);
                    tempList.add(potentialUrl);
                }
            }
        }
        return tempList;
    }

    /**
     * Retrieves the domain String from an url.
     *
     * @param url The url to be processed.
     * @return This url's domain.
     */
    private boolean legitUrl(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
        return !url.isEmpty() && url.length() < 128 && !FILTERS.matcher(url).matches() && !EXCLUDE.matcher(url).matches() && !url.contains("wiki") && !visitedUrls.contains(url);
    }
}
