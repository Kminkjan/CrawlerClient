package message;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageUrl extends Message {
    private String urlData;

    public MessageUrl(String urlData) {
        super(MessageType.GIVE_URL);
        this.urlData = urlData;
    }

    public String getUrlData() {
        return this.urlData;
    }
}
