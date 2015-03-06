package message;

import util.URLData;

/**
 * Created by Kris on 17-2-2015.
 */
public class MessageDone extends Message {
    private final int module;
    private final long ms;
    private final URLData urlData;

    /**
     * Create a Message
     */
    public MessageDone(int module, long ms, URLData urlData) {
        super(MessageType.URL_DONE);
        this.module = module;
        this.ms = ms;
        this.urlData = urlData;
    }

    public int getModule() {
        return module;
    }

    public long getMs() {
        return ms;
    }

    public URLData getUrlData() { return urlData; }
}
