package system;
import java.util.Scanner;

/**
 * Default Commandline application for non-ui systems
 *
 * @author Kris Minkjan
 */
public class Main {

    private static final String WELCOME_MESSAGE = "CrawlerClient v1.1\nThanks for using the crawler made by: \n\n\tTeleCorp(c)\n";

    public static void main(String[] args) {
        System.out.println(WELCOME_MESSAGE);

        Scanner in = new Scanner(System.in);
        System.out.print("amount of threads to start: ");
        int amount = in.nextInt();
        CrawlerSystem system = new CrawlerSystem(amount, args.length > 0 && args[0].equals("verbose"));
        System.out.println("system created \nenter any key to exit");
        in.next();
        system.shutDown();
        System.out.println(WELCOME_MESSAGE);
        in.close();
    }

}
