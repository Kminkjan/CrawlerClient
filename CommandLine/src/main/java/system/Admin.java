package system;

import akka.actor.UntypedActor;
import message.*;
import util.DatabaseConnector;
import util.ServerConnector;
import util.URLData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The admin provides the link between the Modules and the Application thread.
 * <p/>
 * Created by Kris on 16-2-2015.
 */
public class Admin extends UntypedActor {
    /**
     * A buffer to queue up database calls, so that the database wont be spammed with queries
     */
    private final List<URLData> dataBuffer = new ArrayList<URLData>();

    /**
     * Size of the {@link #dataBuffer}
     */
    private static final int BUFFER_SIZE = 200;


    private final DatabaseConnector databaseConnector = new DatabaseConnector();
    private final boolean verbose;
    private final LinkedList<String> activeList = new LinkedList<String>();
    private boolean orderNeeded;
    private ServerConnector serverConnector;

    /**
     * Creates an Admin
     * @param verbose   true if verbose log should be done
     */
    public Admin(boolean verbose) {
        this.verbose = verbose;
        try {
            this.serverConnector = new ServerConnector(getSelf());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        getSender().tell(new MessageActive(activeList.pollFirst(), 2), getSelf());
                    } else if (orderNeeded) {
                        getSender().tell(new MessageOrder(databaseConnector.outdatedDatabaseUrls()), getSelf());
                        orderNeeded = false;
                    }
                }

                for (URLData data : m2.getUrlData() ) {
                    if (verbose) {
                        System.out.println(data.getUrl() + " : " + data.getTag());
                    }
                    /* Add urlData to the databuffer */
                    dataBuffer.add(data);
                }

                if (dataBuffer.size() > BUFFER_SIZE) {
                    databaseConnector.putUrl(dataBuffer);
//                    debugPrint(dataBuffer);
                    dataBuffer.clear();
                }
                break;
            case URL_DONE_ACTIVE:
                System.out.println("PUT RESULT CALLED");
                final MessageDoneActive mda = (MessageDoneActive) message;
//                databaseConnector.putResult(mda.getActiveURLData());
                break;
            case SERVER:
                MessageServer messageServer = (MessageServer) message;
                if (!messageServer.getActiveNeeded().isEmpty()) {
                    activeList.add(messageServer.getActiveNeeded());
                }
                if (!orderNeeded) {
                    this.orderNeeded = messageServer.isRefresh();
                }
                break;
        }
    }

    private void debugPrint(List<URLData> urlDataList) {
        for(URLData urlData : urlDataList) {
            System.out.println(urlData.getTag());
        }
    }
}