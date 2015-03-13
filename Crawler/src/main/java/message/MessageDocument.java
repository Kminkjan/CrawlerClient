package message;

import org.jsoup.nodes.Document;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageDocument extends Message {
    private final Document doc;
    private final int depth;

    public MessageDocument(Document doc, int depth) {
        super(MessageType.DOCUMENT);
        this.doc = doc;
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public Document getDoc() {
        return this.doc;
    }
}
