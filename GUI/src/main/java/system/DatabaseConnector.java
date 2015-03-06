package system;

import util.URLData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
            DATABASE = "telescope_database",
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
        System.out.println("Update: " + constructQuery(urlDataList) + "\nQuery: putUrlData\nTime: "
                + TimeUnit.MILLISECONDS.convert(
                (System.nanoTime() - startTime), TimeUnit.NANOSECONDS));
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
        String query = String.format("INSERT IGNORE INTO url_data (`url`, `tag`, `rating`) VALUES ");
        for (URLData urlData : urlDataList) {
            query += String.format("(\"%s\", \"%s\", %s)", urlData.getUrl(), urlData.getTag(), urlData.getRating()) + ",";
        }
        return query.substring(0, query.length() - 1) + ";"; // trim the last ',' and add ';' to complete te query
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
            query += String.format("(\"%s\", CURDATE())", urlData.getUrl()) + ",";
        }
        query = query.substring(0, query.length() - 1) + " ON DUPLICATE KEY UPDATE timestamp = VALUES(timestamp);";
        return query; // query.substring(0, query.length() - 1) + ";"; // trim the last ',' and add ';' to complete te query
    }


}
