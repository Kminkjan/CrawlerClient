package crawlingmodule;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import util.URLData;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * This is the class where the analysing should be done
 * <p/>
 * #####################################################
 * TODO this is where the algorithm should come
 * ####################################################
 * <p/>
 * Created by KrisMinkjan on 3-3-2015.
 */
public class Analyser {
    /**
     * Words to exclude (Mostly Conjunctions (http://en.wikipedia.org/wiki/Conjunction_(grammar)) and Articles)
     */
    private final static Pattern FILTERS = Pattern.compile("(a|the|an|me|my|i|we|it|for|if|or|but|so|all|as|on|off|thai|" +
            "from|to|of|by)");

    /**
     * Constants to attach values to HTML items.
     */
    private static final int
            META_VALUE = 500,
            URL_VALUE = 250,
            H1_VALUE = 200,
            H2_VALUE = 75,
            LINK_VALUE = 150,
            TEXT_VALUE = 1;


    /**
     * Analyse te Document here. Called by the {@link crawlingmodule.DataProcessor}.
     * <p>First checks for the document's meta content tags, which contain keyword about the webpage's content. Then
     * Analyzes all the H1, H2, H3 and p elements and rates them with values (like {@link #URL_VALUE}). When the
     * document is analysed, the best/most relevant keyword / tag (with the highest accumulated value) is packed int an
     * {@link util.URLData} object and returned</p>
     *
     * @param doc The to be analyzed Document.
     * @return An URLData object that can be put into the Database
     */
    protected URLData analyseDocument(Document doc) {
        TreeMap<String, Integer> map = new TreeMap<String, Integer>();

        /* Check the META keywords */
        for (Element meta : doc.select("meta[name=keywords]")) {
            checkTags(meta.attr("content").split(" "), map, META_VALUE);
        }

        /* Check the META description */
        for (Element meta : doc.select("meta[name=description]")) {
            checkTags(meta.attr("content").split(" "), map, META_VALUE);
        }

        /* Check de document title */
        checkTags(doc.title().split(" "), map, URL_VALUE);

        /* Check rest of the document H1 H2 etc */
        for (Element el : doc.select("h1")) {
            checkTags(el.text().split(" "), map, H1_VALUE);
        }
        for (Element el : doc.select("h2")) {
            checkTags(el.text().split(" "), map, H2_VALUE);
        }
        for (Element el : doc.select("p")) {
            checkTags(el.text().split(" "), map, TEXT_VALUE);
        }
        for (Element el : doc.select("a")) {
            checkTags(el.text().split(" "), map, LINK_VALUE);
        }

        /* if there are any keywords, return the most relevant */
        if (!map.isEmpty()) {
            return constructURLData(map.entrySet(), doc.location());
        } else {
            return null;
        }
    }

    /**
     * Checks all the given tags and add value to them.
     *
     * @param tags  The tags
     * @param map   The map where the tags and their values are stored
     * @param VALUE The value to add to a tag
     */
    private void checkTags(String[] tags, Map<String, Integer> map, final int VALUE) {
        for (String tag : tags) {
            /* trim all the irrelevant characters*/
            tag = tag.replaceAll("[,|.|:|;|!|?|(|)|^|+|/]", "").trim().toLowerCase();

            /* Add the counter for that tag */
            if (!tag.isEmpty() && !FILTERS.matcher(tag).matches()) {
                if (map.containsKey(tag)) {
                    map.put(tag, map.get(tag) + VALUE);
                } else {
                    map.put(tag, VALUE);
                }
            }
        }
    }

    /**
     * Constructs an {@link util.URLData} object from the most relevant tag.
     *
     * @param entrySet All the tags with their values
     * @param link     The link (url) of the source web page
     * @return A new URLData containing the tag's information
     */
    private URLData constructURLData(Set<Map.Entry<String, Integer>> entrySet, String link) {
        TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>();

        /* Put every entry in a TreeMap sorted to values instead of Strings */
        for (Map.Entry<String, Integer> entry : entrySet) {
            treeMap.put(entry.getValue(), entry.getKey());
        }

        Map.Entry<Integer, String> entry = treeMap.pollLastEntry();
        while (entry.getValue().isEmpty() && !treeMap.isEmpty()) {
            entry = treeMap.pollLastEntry();
        }

        return new URLData(link, entry.getValue(), entry.getKey());
    }
}
