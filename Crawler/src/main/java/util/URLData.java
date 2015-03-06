package util;

/**
 * Contains data ready to be put in the database.
 * Created by Kris on 4-3-2015.
 */
public class URLData {
    private final String url, tag;
    private final int rating;

    public URLData(String url, String tag, int rating) {
        this.url = url;
        this.tag = tag;
        this.rating = rating;
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
}
