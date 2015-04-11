package util;

import javafx.beans.property.SimpleStringProperty;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by KrisMinkjan on 14-2-2015.
 */
public class ModuleInfo {
    private final SimpleStringProperty id;
    private final SimpleStringProperty status;

    /**
     * The 100 last crawl results. Used to calculate the url/minute in the GUI, see {@link #calculateUrlMin(long)}.
     */
    private LinkedList<Long> timesList = new LinkedList<Long>();

    public SimpleStringProperty currentUrlProperty() {
        return currentUrl;
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    private final SimpleStringProperty currentUrl;

    public String getPerformance() {
        return performance.get();
    }

    public SimpleStringProperty performanceProperty() {
        return performance;
    }

    public void setPerformance(long performance) {
        this.performance.set(calculateUrlMin(performance) + "");
    }

    private final SimpleStringProperty performance;

    public ModuleInfo(String id, String status, String currentUrl, String performance) {
        this.id = new SimpleStringProperty(id);
        this.status = new SimpleStringProperty(status);
        this.currentUrl = new SimpleStringProperty(currentUrl);
        this.performance = new SimpleStringProperty(performance);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getCurrentUrl() {
        return currentUrl.get();
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl.set(currentUrl);
    }

    /**
     * Calculates the performance of this {@link crawlingmodule.Module} by calculating the average
     * time per result of the last 100 results and divide it by 60000 (a minute).
     *
     * @param result The amount of time of the last result.
     * @return The average amount of urls per minute.
     */
    private long calculateUrlMin(long result) {
        timesList.push(TimeUnit.MILLISECONDS.convert(result, TimeUnit.NANOSECONDS));

        /* drop the last value */
        if (timesList.size() > 100) {
            timesList.removeLast();
        }

        Long performance = 1l;
        for (Long l : timesList) {
            performance += l;
        }

        return 60000 / (performance / timesList.size());
    }
}
