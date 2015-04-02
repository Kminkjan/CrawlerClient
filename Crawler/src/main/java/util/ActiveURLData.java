package util;

import java.util.List;

/**
 * Created by Kris on 11-3-2015.
 */
public class ActiveURLData extends URLData implements Comparable<ActiveURLData> {
    private final int depth;
    private int searchId;
    private String tag;
    private final List<String> linkList;

    public ActiveURLData(String url, String tag, int rating,int depth, List<String> linkList) {
        super(url, tag, rating);
        this.depth = depth;
        this.linkList = linkList;
    }

    public int getDepth() {
        return depth;
    }

    public List<String> getLinkList() {
        return linkList;
    }

    @Override
    public int compareTo(ActiveURLData d) {
        return this.depth - d.getDepth();
    }

    public int getSearchId() {
        return searchId;
    }

    public void setSearchId(int searchId) {
        this.searchId = searchId;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
