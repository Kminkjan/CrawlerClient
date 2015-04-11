package util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Googler class googles domains to crawl.
 * TODO maybe integrate this with the regular parsing
 *
 * Created by Kris on 9-4-2015.
 */
public class Googler {
    private static Pattern patternDomainName;
    private Matcher matcher;
    private static final String DOMAIN_NAME_PATTERN
            = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
    static {
        patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
    }

    private String getDomainName(String url){

        String domainName = "";
        matcher = patternDomainName.matcher(url);
        if (matcher.find()) {
            domainName = matcher.group(0).toLowerCase().trim();
        }
        return domainName;

    }

    public Set<String> getDataFromGoogle(String query) {

        Set<String> result = new HashSet<String>();
        String request = "https://www.google.com/search?q=" + "cheese" + "&num=3";
        System.out.println("Sending request..." + query);

        try {

            // need http protocol, set this as a Google bot agent :)
            Document doc = Jsoup
                    .connect(query)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();

            // get all links
            Elements links = doc.select("a[href]");
            for (Element link : links) {

                String temp = link.attr("href");
                System.out.println(temp);
                if(temp.startsWith("/url?q=")){
                    //use regex to get domain name
                    result.add("http://" + getDomainName(temp));
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
