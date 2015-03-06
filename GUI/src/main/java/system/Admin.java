package system;

import akka.actor.UntypedActor;
import javafx.application.Platform;
import message.Message;
import message.MessageDone;
import util.URLData;

import java.util.ArrayList;
import java.util.List;

/**
 * The admin provides the link between the Modules and the Application thread.
 * <p/>
 * Created by Kris on 16-2-2015.
 */
public class Admin extends UntypedActor {
    private final CrawlerSystem system;
    private final List<URLData> dataBuffer = new ArrayList<URLData>();
    private static final int BUFFER_SIZE = 200;
    private final DatabaseConnector databaseConnector = new DatabaseConnector();

    public Admin(CrawlerSystem system) {
        this.system = system;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case URL_DONE:
                final MessageDone m2 = (MessageDone) message;

                /* Add urlData to the databuffer */
                dataBuffer.add(m2.getUrlData());
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
        }
    }

    private void debugPrint(List<URLData> urlDataList) {
        for(URLData urlData : urlDataList) {
            System.out.println(urlData.getTag());
        }
    }
}
