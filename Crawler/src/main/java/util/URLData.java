package util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Contains data ready to be put in the database.
 * Created by Kris on 4-3-2015.
 */
public class URLData {
    private final String url, tag;
    private String domain;
    private final int rating;

    public URLData(String url, String tag, int rating) {
        this.url = url;
        this.tag = tag;
        this.rating = rating;
        try {
            this.domain = getDomain(url);
        } catch (URISyntaxException ignored) {
            this.domain = url;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getTag() {
        return tag;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "URLData{" +
                "url='" + url + '\'' +
                ", tag='" + tag + '\'' +
                ", rating=" + rating +
                '}';
    }

    /**
     * Retrieves the domain String from an url.
     *
     * @param url The url to be processed.
     * @return This url's domain.
     * @throws java.net.URISyntaxException
     */
    private String getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            System.out.println("null url: " + url);
            return null;
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public String getDomain() {
        return domain;
    }
}
