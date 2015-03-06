package message;

import org.jsoup.nodes.Document;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageDocument extends Message {
    private final Document doc;

    public MessageDocument(Document doc) {
        super(MessageType.DOCUMENT);
        this.doc = doc;
    }

    public Document getDoc() {
        return this.doc;
    }
}
