package system;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import message.MessageDone;
import util.CAdmin;
import util.CSystem;

/**
 * Created by Kris on 11-4-2015.
 */
public class GUIAdmin extends CAdmin {
    public GUIAdmin(CSystem system, SimpleStringProperty connectionProperty) {
        super(system, connectionProperty);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        super.onReceive(o);
        final MessageDone m2 = (MessageDone) o;

        /* TODO Update UI */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getSystem().updateInfo(m2.getModule(), m2.getMs());
            }
        });

    }
}
