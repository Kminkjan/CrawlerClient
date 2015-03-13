package util;

/**
 * Created by Kris on 10-3-2015.
 */
public class DepthData implements Comparable<DepthData> {
    private String url;
    private int depth;

    public DepthData(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public int compareTo(DepthData d) {
        return this.depth - d.getDepth();
    }
}
