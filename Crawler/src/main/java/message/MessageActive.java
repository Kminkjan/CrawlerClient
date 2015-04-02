package message;

/**
 * Created by Kris on 10-3-2015.
 */
public class MessageActive extends Message {
    private final String startUrl, tag;
    private final int maxDepth, searchId;

    /**
     * Create a Message
     *
     * @param startUrl the url where to start to crawl
     * @param maxDepth How deep to crawl
     */
    public MessageActive(String startUrl, int maxDepth, int searchId, String tag) {
        super(MessageType.ACTIVE);
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.searchId = searchId;
        this.tag = tag;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getSearchId() {
        return searchId;
    }

    public String getTag() {
        return tag;
    }
}
