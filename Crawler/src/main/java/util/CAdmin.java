package util;

import akka.actor.UntypedActor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import message.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kris on 11-4-2015.
 */
public class CAdmin extends UntypedActor{
    private final CSystem system;
    private final List<URLData> dataBuffer;
    private static final int BUFFER_SIZE = 5000;
    private final DatabaseConnector databaseConnector = new DatabaseConnector();

    /* Server comms stuff */
    private final LinkedList<MessageActive> activeList;
    private boolean orderNeeded;
    private ServerConnector serverConnector;
    private final UICallable uiCallable;


    // TODO the property
    public CAdmin(CSystem system, UICallable uiCallable, ServerConnector serverConnector) {
        this.system = system;
        this.serverConnector = serverConnector;
        this.uiCallable = uiCallable;
        activeList = new LinkedList<>();
        dataBuffer = new ArrayList<>();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        serverConnector.tellServer("shutdown");
        serverConnector.stopConnection();
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
                    dataBuffer.clear();
                }

                uiCallable.updateInfo(m2.getModule(), m2.getMs());

                break;
            case URL_DONE_ACTIVE:
                System.out.println("PUT RESULT CALLED");
                final MessageDoneActive mda = (MessageDoneActive) message;
                databaseConnector.putResult(mda.getActiveURLData());
                break;
            case SERVER:
                MessageServer messageServer = (MessageServer) message;
                if (!orderNeeded) {
                    this.orderNeeded = messageServer.isRefresh();
                }
                break;
            case ACTIVE:
                activeList.add((MessageActive) message);
                break;
            case MODULE_NOTIFY: // done crawling active
                serverConnector.tellServer("addthread");
                break;
        }
    }

    protected CSystem getSystem() {
        return system;
    }
}
