package message;

import util.URLData;

import java.util.List;

/**
 * Created by Kris on 17-2-2015.
 */
public class MessageDone extends Message {
    private final int module;
    private final long ms;
    private final List<URLData> urlData;
    private final boolean available;

    /**
     * Create a Message
     */
    public MessageDone(int module, long ms, List<URLData> urlData, boolean available) {
        super(MessageType.URL_DONE);
        this.module = module;
        this.ms = ms;
        this.urlData = urlData;
        this.available = available;
    }

    public int getModule() {
        return module;
    }

    public long getMs() {
        return ms;
    }

    public List<URLData> getUrlData() { return urlData; }

    public boolean isAvailable() {
        return available;
    }
}
