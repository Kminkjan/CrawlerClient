package crawlingmodule;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import message.Message;
import message.MessageDocument;
import message.MessageProcess;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpe?g"
            + "|png|mp3|mp3|zip|gz|exe|jar))$");

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
                processDocument(((MessageDocument) message).getDoc());
                break;
        }
    }

    /**
     * processes the document.
     *
     * @param doc The document to process
     */
    private void processDocument(Document doc) {
        if (doc == null) {
            System.out.println("document is null");
        } else {
            URLData urlData = analyser.analyseDocument(doc);
            if (urlData != null) {
                module.tell(new MessageProcess(selectUrls(doc.select("a")), urlData), getSelf());
            }
        }
    }


    /**
     * Select some urls from the document to send to the {@link Module}.
     *
     * @param elements The elements to choose from.
     */
    private List<String> selectUrls(Elements elements) {
        List<String> tempList = new ArrayList<String>();
        Random random = new Random();
        int elementCount = elements.size();

        if (elementCount > 0) {
            /* Try to select 5 urls */
            for (int i = 0; i < 5; i++) {
                String potentialUrl = elements.get(random.nextInt(elementCount)).attr("abs:href");
                if (potentialUrl != null && !potentialUrl.isEmpty() && legitUrl(potentialUrl) && !potentialUrl.contains("wiki") && !potentialUrl.contains("#") && !potentialUrl.contains("?") && !visitedUrls.contains(potentialUrl)) {

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
        return !FILTERS.matcher(url).matches();
    }
}
