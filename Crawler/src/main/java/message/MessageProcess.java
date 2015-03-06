package message;

import util.URLData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageProcess extends Message {
    private List<String> urlList;
    private final URLData urlData;

    public MessageProcess(List<String> urlList,URLData urlData) {
        super(MessageType.PROCESSED_URLS);

        /* Make the urlList immutable */
        this.urlList = Collections.unmodifiableList(new ArrayList<String>(urlList));
        this.urlData = urlData;
    }

    public List<String> getUrlList() {
        return this.urlList;
    }

    public URLData getUrlData() {
        return urlData;
    }
}
