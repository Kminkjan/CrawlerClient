package message;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class Message {

    /**
     * Enum that describes the content of the message.
     */
    public enum MessageType {
        EDIT_VALUE, PROCESSED_URLS, DOCUMENT, CRAWLER_NOTIFY, PROCESSOR_NOTIFY, MODULE_NOTIFY, REQUEST_URL, GIVE_URL, SHUT_DOWN, URL_DONE
    }

    /**
     * Content of the message.
     */
    private final MessageType type;

    /**
     * Create a Message
     *
     * @param type The type of Message {@link message.Message.MessageType}.
     */
    public Message(MessageType type) {
        this.type = type;
    }

    /**
     * Get the type of Message
     *
     * @return The {@link message.Message.MessageType} of this Message.
     */
    public final MessageType getType() {
        return this.type;
    }

}
