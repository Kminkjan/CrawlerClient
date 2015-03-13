package message;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageUrl extends Message {
    private final String urlData;
    private final int depth;

    public MessageUrl(String urlData, int depth) {
        super(MessageType.GIVE_URL);
        this.urlData = urlData;
        this.depth = depth;
    }

    public String getUrlData() {
        return this.urlData;
    }

    public int getDepth() {
        return depth;
    }
}
