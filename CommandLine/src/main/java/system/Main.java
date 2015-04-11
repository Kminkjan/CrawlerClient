package system;

/**
 * Default Commandline application for non-ui systems
 *
 * @author Kris Minkjan
 */
public class Main{

    public static void main(String[] args) {
        new UIController().start();
    }
}
