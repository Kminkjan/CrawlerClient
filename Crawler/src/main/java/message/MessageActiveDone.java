package message;

import util.ActiveURLData;

/**
 * Created by Kris on 12-3-2015.
 */
public class MessageActiveDone extends Message {
    private ActiveURLData activeURLData;

    /**
     * Create a Message
     *
     */
    public MessageActiveDone(ActiveURLData activeURLData) {
        super(MessageType.ACTIVE_PROCESS);
        this.activeURLData = activeURLData;
    }

    public ActiveURLData getActiveURLData() {
        return activeURLData;
    }
}
