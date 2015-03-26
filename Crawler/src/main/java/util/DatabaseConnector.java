package util;

import util.URLData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides a connection to the database
 * Created by KrisMinkjan on 3-3-2015.
 */
public class DatabaseConnector {
    /**
     * String constants for the database connection
     */
    private final static String
            HOST = "jdbc:mysql://178.21.117.113:3306/",
            DATABASE = "telescope_db3",
            USERNAME = "rooter",
            PASSWORD = "haeshah3";

    public DatabaseConnector() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
            System.out.println(constructPreQuery(urlDataList));
            statement.executeUpdate(constructPreQuery(urlDataList));

            statement.close();

            statement = connection.createStatement();
            statement.executeUpdate(constructQuery(urlDataList));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (statement != null) statement.close();} catch (Exception e) {}
            try { if (connection != null) connection.close();} catch (Exception e) {}
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
            System.out.println("SELECT url FROM url WHERE " + System.currentTimeMillis() + " - timestamp > " + 7/*1000 * 60 * 60 * 24 *7*/ + ";");
            resultset = statement.executeQuery("SELECT url FROM url WHERE " + System.currentTimeMillis() + " - timestamp > " + 7/*1000 * 60 * 60 * 24 *7*/ + ";");
            while (resultset.next()) {
                updateList.add("http://" + resultset.getString("url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {if (resultset != null) resultset.close();} catch (Exception e) {};
            try {if (statement != null) statement.close();} catch (Exception e) {};
            try {if (connection != null) connection.close();} catch (Exception e) {};
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
                System.out.println(constructResultQuery(activeURLData));
                statement.executeUpdate(constructResultQuery(activeURLData));
                System.out.println(constructHyperlinkQuery(activeURLData.getLinkList(), activeURLData.getUrl()));
                statement.executeUpdate(constructHyperlinkQuery(activeURLData.getLinkList(), activeURLData.getUrl()));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try { if (statement != null) statement.close();} catch (Exception e) {}
                try { if (connection != null) connection.close();} catch (Exception e) {}
            }
            System.out.println("Send ACTIVE! data to DataBase" + "\nQuery: putUrlData\n\tTime: "
                    + TimeUnit.MILLISECONDS.convert(
                    (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));
    }

    private String constructResultQuery(ActiveURLData data) {
        return String.format("INSERT INTO search_result (`searchid`,`tag`,`completeurl`,`orginurl`,`rating`,`pagecolor`," +
                "`depth`,`crawlername`) VALUES (%s, \"%s\", \"%s\", \"%s\", %s,  \"%s\", %s, \"%s\")", 1, "kris", data.getUrl(), "nourl",
                data.getRating() + (Math.random() * 50), "ffffff", data.getDepth(), "testcrawler");
    }

    private String constructHyperlinkQuery(List<String> hyperlinks, String sourceUrl) {
        String query = "INSERT INTO hyperlink (`completeurl`,`hyperlink`) VALUES ";
        for (String hyperlink : hyperlinks) {
            query += String.format("(\"%s\", \"%s\"),", sourceUrl, hyperlink);
        }
        return query.substring(0, query.length() - 1) + ";";
    }
}
