package system;

import util.CSystem;
import util.UICallables;

import java.util.Scanner;

/**
 * Created by KrisMinkjan on 11-4-2015.
 */
public class UIController implements UICallables {

    public void start() {
        String WELCOME_MESSAGE = "CrawlerClient v1.1\nThanks for using the crawler made by: \n\n\tTeleCorp(c)\n";
        System.out.println(WELCOME_MESSAGE);

        Scanner in = new Scanner(System.in);
        System.out.print("amount of threads to start: ");
        int amount = in.nextInt();
        CSystem system = new CSystem(this);
        for (int i = 0; i < amount; i++) {
            system.addModule();
        }
        System.out.println("system created \nenter any key to exit");
        in.next();
        system.shutDown();
        System.out.println(WELCOME_MESSAGE);
        in.close();
    }

    @Override
    public void updateInfo(int module, long ms) {
        // Lol ignore
    }

    @Override
    public void updateConnectionStatus(String status) {
        System.out.println("Connection status: " + status);
    }
}
