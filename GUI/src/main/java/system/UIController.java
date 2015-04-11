package system;

import javafx.application.Platform;
import util.CSystem;
import util.ModuleInfo;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import util.UICallables;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The UIController controls the JavaFX User interface. JavaFX are injected by the @FXML annotation.
 *
 * Created by KrisMinkjan on 13-2-2015.
 */
public class UIController implements Initializable, UICallables {

    @FXML
    private Button addButton;
    @FXML
    private Button terButton;
    @FXML
    private Label threadCountLabel;
    @FXML
    private Label urlmin;
    @FXML
    private Label connectionstatus;
    @FXML
    private Label totalurls;
    @FXML
    private TableView<ModuleInfo> tableView;
    @FXML
    private Slider powerSlider;
    @FXML
    private LineChart<Integer, Integer> lineChart;

    private final SimpleStringProperty urlminProperty = new SimpleStringProperty(""), urltotalProperty = new SimpleStringProperty(""),
            connectionStatusProperty = new SimpleStringProperty("");
    private HashMap<Integer, ModuleInfo> infoHashMap = new HashMap<Integer, ModuleInfo>();
    private int count, idcounter;
    private static CSystem system;

    private ObservableList<XYChart.Data> dataList =
            FXCollections.observableArrayList(
                    new XYChart.Data("00:00:00", 100));
    private int urlsProcessed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        system = new CSystem(this);

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                System.out.println("Added Thread");
                threadCountLabel.setText("" + ++count);

                ModuleInfo info = system.addModule();
                ObservableList<ModuleInfo> data = tableView.getItems();
                data.add(info);
                infoHashMap.put(idcounter++, info);
            }
        });

        terButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
//                ObservableList<ModuleInfo> data = tableView.getItems();
//                if (system.removeModule()) {
//                    System.out.println("Marked Thread for termination");
//                    threadCountLabel.setText("" + --count);
//                } else {
//                    System.out.println("You cant delete a thread if there isn't any active thread");
//                }
                //system.activate("https://java.com/nl/download/");
//                system.activate("http://www.jsoup.org");
//                system.refresh();
                system.shutDown();
            }
        });

//        powerSlider.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//
//                system.setDelay(newValue.intValue());
//            }
//        });

        XYChart.Series series = new XYChart.Series(dataList);
        series.setName("XYChart.Series");
        lineChart.getData().add(series);

        dataList.remove(0);

        urlmin.textProperty().bind(urlminProperty);
        totalurls.textProperty().bind(urltotalProperty);
        connectionstatus.textProperty().bind(connectionStatusProperty);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                updateUI();
                            }
                        }
                ),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();


    }

    /**
     * Stops the system
     */
    public static void stop() {
        system.shutDown();
    }

    @Override
    public void updateInfo(final int module, final long ms) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                infoHashMap.get(module).setPerformance(ms);
                ++urlsProcessed;
            }
        });

    }

    @Override
    public void updateConnectionStatus(final String status) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                connectionStatusProperty.set(status);
            }
        });
    }

    public void updateUI() {
        int sum = 0;
        for (ModuleInfo info : infoHashMap.values()) {
            sum += Integer.parseInt(info.getPerformance());
        }
        urlminProperty.set("" + sum);
        urltotalProperty.set("" + urlsProcessed);
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), sum));
//        dataList.add(new XYChart.Data(sdf.format(cal.getTime()), urlsProcessed));
        if (dataList.size() > 20) {
            dataList.remove(0);
        }
    }
}
