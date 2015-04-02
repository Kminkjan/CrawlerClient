package system;

import akka.actor.UntypedActor;
import javafx.application.Platform;
import message.*;
import util.DatabaseConnector;
import util.ServerConnector;
import util.URLData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The admin provides the link between the Modules, the database and the Application thread.
 * <p/>
 * Created by Kris on 16-2-2015.
 */
public class Admin extends UntypedActor {
    private final CrawlerSystem system;
    private final List<URLData> dataBuffer = new ArrayList<URLData>();
    private static final int BUFFER_SIZE = 200;
    private final DatabaseConnector databaseConnector = new DatabaseConnector();
    /* Server comms stuff */
    private final LinkedList<MessageActive> activeList = new LinkedList<MessageActive>();
    private boolean orderNeeded;
    private ServerConnector serverConnector;

    public Admin(CrawlerSystem system) {
        this.system = system;
        this.serverConnector = new ServerConnector(getSelf());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        serverConnector.stop();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case URL_DONE:
                final MessageDone m2 = (MessageDone) message;
                /* Check for availability */
                if (m2.isAvailable()) {
                    if (!activeList.isEmpty()) {
                        getSender().tell(activeList.pollFirst(), getSelf());
                    } else if (orderNeeded) {
                        getSender().tell(new MessageOrder(databaseConnector.outdatedDatabaseUrls()), getSelf());
                        orderNeeded = false;
                    }
                }

                /* Add urlData to the databuffer */
                dataBuffer.addAll(m2.getUrlData());
                if (dataBuffer.size() > BUFFER_SIZE) {
                    databaseConnector.putUrl(dataBuffer);
//                    debugPrint(dataBuffer);
                    dataBuffer.clear();
                }

                /* Update UI */
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        system.updateInfo(m2.getModule(), m2.getMs());
                    }
                });

                break;
            case URL_DONE_ACTIVE:
                System.out.println("PUT RESULT CALLED");
                final MessageDoneActive mda = (MessageDoneActive) message;
                databaseConnector.putResult(mda.getActiveURLData());
                break;
            case SERVER:
                MessageServer messageServer = (MessageServer) message;
                if (!messageServer.getActiveNeeded().isEmpty()) {
                    // activeList.add(messageServer.getActiveNeeded());
                }
                if (!orderNeeded) {
                    this.orderNeeded = messageServer.isRefresh();
                }
                break;
            case ACTIVE:
                activeList.add((MessageActive) message);
        }
    }

    private void debugPrint(List<URLData> urlDataList) {
        for (URLData urlData : urlDataList) {
            System.out.println(urlData.getUrl() + " " + urlData.getTag());
        }
    }
}
