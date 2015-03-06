package system; /**
 * Created by KrisMinkjan on 13-2-2015.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Thanks for using the crawler made by: \n\n\tTeleCorp\n");

                Scanner in = new Scanner(System.in);
        System.out.print("Amount of threads to start: ");
        int amount = in.nextInt();
        in.close();
        CrawlerSystem system = new CrawlerSystem(amount);
    }

}
