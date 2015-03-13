package message;

import util.ActiveURLData;
import util.URLData;

import java.util.List;

/**
 * Created by Kris on 17-2-2015.
 */
public class MessageDoneActive extends Message {
    private final int module;
    private final long ms;
    private final ActiveURLData activeURLData;

    /**
     * Create a Message
     */
    public MessageDoneActive(int module, long ms, ActiveURLData activeURLData) {
        super(MessageType.URL_DONE_ACTIVE);
        this.module = module;
        this.ms = ms;
        this.activeURLData = activeURLData;
    }

    public int getModule() {
        return module;
    }

    public long getMs() {
        return ms;
    }

    public ActiveURLData getActiveURLData() {
        return activeURLData;
    }
}
