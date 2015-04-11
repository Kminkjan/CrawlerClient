package util;

import util.URLData;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Provides a connection to the database
 * Created by KrisMinkjan on 3-3-2015.
 */
public class DatabaseConnector {

    private final static Logger LOGGER = Logger.getLogger(DatabaseConnector.class.getName());

    /**
     * String constants for the database connection
     */
    private final static String
            HOST = "jdbc:mysql://192.168.1.83:3306/",
            DATABASE = "Telescope",
            USERNAME = "crawler",
            PASSWORD = "sdLeYHNdaRWjy5mn";

    public DatabaseConnector() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Puts data from a <b>Crawler</b> in the database.
     */
    public void putUrl(List<URLData> urlDataList) {
        long startTime = System.nanoTime();

        Connection connection = null;
        Statement statement = null;
        try {
            connection = getDBConnection();
            statement = connection.createStatement();
            //System.out.println(constructPreQuery(urlDataList));
            statement.executeUpdate(constructPreQuery(urlDataList));

            statement.close();

            statement = connection.createStatement();
            statement.executeUpdate(constructQuery(urlDataList));
        } catch (SQLException e) {
            LOGGER.warning(e.getLocalizedMessage());
        } finally {
            try { if (statement != null) statement.close();} catch (Exception ignored) {}
            try { if (connection != null) connection.close();} catch (Exception ignored) {}
        }
        System.out.println("Send data to DataBase" + "\nQuery: putUrlData\n\tTime: "
                + TimeUnit.MILLISECONDS.convert(
                (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));
    }

    public List<String> outdatedDatabaseUrls() {
        long startTime = System.nanoTime();

        List<String> updateList = new ArrayList<String>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultset = null;
        try {
            connection = getDBConnection();
            statement = connection.createStatement();
            //System.out.println("SELECT url FROM url WHERE " + System.currentTimeMillis() + " - timestamp > " + 7/*1000 * 60 * 60 * 24 *7*/ + ";");
            resultset = statement.executeQuery("SELECT url FROM url WHERE " + System.currentTimeMillis() + " - timestamp > " + 7/*1000 * 60 * 60 * 24 *7*/ + ";");
            while (resultset.next()) {
                updateList.add("http://" + resultset.getString("url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {if (resultset != null) resultset.close();} catch (Exception ignored) {};
            try {if (statement != null) statement.close();} catch (Exception ignored) {};
            try {if (connection != null) connection.close();} catch (Exception ignored) {};
        }
        System.out.println("Update:\nQuery: updateDatabase\nTime: "
                + TimeUnit.MILLISECONDS.convert(
                (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));
        return updateList;
    }

    /**
     * Gets the SQL database connection
     *
     * @return The SQL database connection
     * @throws SQLException
     */
    private Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(HOST + DATABASE, USERNAME, PASSWORD);
    }

    /**
     * Constructs a query from a List.
     *
     * @param urlDataList The list items that will be put into the database.
     * @return The query String
     */
    private String constructQuery(List<URLData> urlDataList) {
        String query = String.format("INSERT IGNORE INTO url_data (`url`, `tag`, `rating`,`domain`) VALUES ");
        for (URLData urlData : urlDataList) {
            query += String.format("(\"%s\", \"%s\", %s, \"%s\")", urlData.getUrl(), urlData.getTag(), urlData.getRating(), urlData.getDomain()) + ",";
        }
        return query.substring(0, query.length() - 1) + ";";
    }

    /**
     * Constructs a query from a List.
     *
     * @param urlDataList The list items that will be put into the database.
     * @return The query String
     */
    private String constructPreQuery(List<URLData> urlDataList) {
        String query = String.format("INSERT INTO url (`url`, `timestamp`) VALUES ");
        for (URLData urlData : urlDataList) {
            query += String.format("(\"%s\", UNIX_TIMESTAMP(NOW()))", urlData.getUrl()) + ",";
        }
        query = query.substring(0, query.length() - 1) + " ON DUPLICATE KEY UPDATE timestamp = VALUES(timestamp);";
        return query;
    }

    public void putResult(ActiveURLData activeURLData) {
            long startTime = System.nanoTime();

            Connection connection = null;
            Statement statement = null;
            try {
                connection = getDBConnection();
                statement = connection.createStatement();
                //System.out.println(constructResultQuery(activeURLData));
                statement.executeUpdate(constructResultQuery(activeURLData));
                //System.out.println(constructHyperlinkQuery(activeURLData.getLinkList(), activeURLData.getUrl()));

                //statement.executeUpdate(constructHyperlinkQuery(activeURLData.getLinkList(), activeURLData.getUrl()));
                //System.out.println(constructHyperlinkQuery(activeURLData.getSearchId(), activeURLData.getLinkList(), activeURLData.getDomain()));
                statement.executeUpdate(constructHyperlinkQuery(activeURLData.getSearchId(), activeURLData.getLinkList(), activeURLData.getDomain()));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try { if (statement != null) statement.close();} catch (Exception ignored) {}
                try { if (connection != null) connection.close();} catch (Exception ignored) {}
            }
            System.out.println("Send ACTIVE! data to DataBase" + "\nQuery: putUrlData\n\tTime: "
                    + TimeUnit.MILLISECONDS.convert(
                    (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));
    }

    private String constructResultQuery(ActiveURLData data) {
        return String.format("INSERT INTO search_result (`searchid`,`tag`,`completeurl`,`orginurl`,`rating`,`pagecolor`," +
                "`depth`,`crawlername`,`domain`) VALUES (%s, \"%s\", \"%s\", \"%s\", %s,  \"%s\", %s, \"%s\", \"%s\")", data.getSearchId(), data.getTag(), data.getUrl(), "nourl",
                data.getRating() + (Math.random() * 50), "ffffff", data.getDepth(), "testcrawler", data.getDomain());
    }

    private String constructHyperlinkQuery(List<String> hyperlinks, String sourceUrl) {
        String query = "INSERT IGNORE INTO hyperlink (`completeurl`,`hyperlink`) VALUES ";
        for (String hyperlink : hyperlinks) {
            query += String.format("(\"%s\", \"%s\"),", sourceUrl, hyperlink);
        }
        return query.substring(0, query.length() - 1) + ";";
    }

    private String constructHyperlinkQuery(int searchId, List<String> hyperlinks, String sourceDomain) {
        String query = "INSERT IGNORE INTO hyperlink (`searchid`, `domain`,`hyperlink`, `amount`) VALUES ";
        for (String hyperlink : hyperlinks) {
            try {
                query += String.format("(%d, \"%s\", \"%s\", %d),",searchId, sourceDomain, getDomain(hyperlink), 1);
            } catch (URISyntaxException ignored) {}
        }
        return query.substring(0, query.length() - 1) + " ON DUPLICATE KEY UPDATE amount = amount + 1;";
    }

    public List<String> getRandomUrl() {
        String query = "SELECT url FROM `url_data` ORDER BY RAND() LIMIT 1;";

        long startTime = System.nanoTime();

        List<String> updateList = new ArrayList<String>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultset = null;
        try {
            connection = getDBConnection();
            statement = connection.createStatement();
            resultset = statement.executeQuery("SELECT url FROM url_data ORDER BY RAND() LIMIT 5;");
            while (resultset.next()) {
//                System.out.println(resultset.getString(1));
                updateList.add(resultset.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {if (resultset != null) resultset.close();} catch (Exception ignored) {}
            try {if (statement != null) statement.close();} catch (Exception ignored) {}
            try {if (connection != null) connection.close();} catch (Exception ignored) {}
        }

        for(String s : updateList) {
            System.out.println(s);
        }

        System.out.println("Update:\nQuery: randomurl\nTime: "
                + TimeUnit.MILLISECONDS.convert(
                (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));

        return updateList;
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
}
