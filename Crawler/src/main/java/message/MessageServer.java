package message;

/**
 * Created by Kris on 13-3-2015.
 */
public class MessageServer extends Message {
    private final String activeNeeded;
    private final boolean refresh;

    /**
     * Create a Message
     *
     * @param type The type of Message {@link message.Message.MessageType}.
     */
    public MessageServer(String activeNeeded, boolean refresh) {
        super(MessageType.SERVER);
        this.activeNeeded = activeNeeded;
        this.refresh = refresh;
    }

    public String getActiveNeeded() {
        return activeNeeded;
    }

    public boolean isRefresh() {
        return refresh;
    }
}
