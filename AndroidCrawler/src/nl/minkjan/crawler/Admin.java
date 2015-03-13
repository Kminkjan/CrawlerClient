package nl.minkjan.crawler;

import akka.actor.UntypedActor;
import android.app.Activity;
import message.Message;
import message.MessageDone;
import message.MessageDoneActive;
import util.DatabaseConnector;
import util.URLData;

import java.util.ArrayList;
import java.util.List;

/**
 * The admin provides the link between the Modules and the Application thread.
 * <p/>
 * Created by Kris on 16-2-2015.
 */
public class Admin extends UntypedActor {
    private final List<URLData> dataBuffer = new ArrayList<URLData>();
    private static final int BUFFER_SIZE = 200;
    private final DatabaseConnector databaseConnector = new DatabaseConnector();
    private final MainActivity activity;
    private int count = 0;

    public Admin(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        Message message = (Message) o;
        switch (message.getType()) {
            case URL_DONE:
                final MessageDone m2 = (MessageDone) message;
                count += m2.getUrlData().size();
                for (URLData data : m2.getUrlData() ) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.setTextView(count);
                        }
                    });

                    /* Add urlData to the databuffer */
                    dataBuffer.add(data);
                }

                if (dataBuffer.size() > BUFFER_SIZE) {
                    databaseConnector.putUrl(dataBuffer);
//                    debugPrint(dataBuffer);
                    dataBuffer.clear();
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
