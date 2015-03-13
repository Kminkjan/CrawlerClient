package message;

/**
 * Created by Kris on 10-3-2015.
 */
public class MessageActive extends Message {
    private final String startUrl;
    private final int maxDepth;

    /**
     * Create a Message
     *
     * @param startUrl the url where to start to crawl
     * @param maxDepth How deep to crawl
     */
    public MessageActive(String startUrl, int maxDepth) {
        super(MessageType.ACTIVE);
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
