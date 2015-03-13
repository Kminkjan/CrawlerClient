package util;

import akka.actor.ActorRef;
import message.MessageActive;
import message.MessageOrder;
import message.MessageServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * The ServerConnector provides the connection to the Server. Documentation regarding this matter is found here:
 * <url>https://docs.google.com/document/d/1HqIMG1F1CbjBwrKhs9wc1DS-xlNcbD17wPtYfaCB0pE/edit?usp=sharing</url>.
 * <p/>
 * Created by Kris Minkjan on 6-3-2015.
 *
 * @author Kris Minkjan
 * @author Roel Kolkhuis Tanke
 */
public class ServerConnector {

    private final static String SERVER_ADRESS = "localhost";
    private final static int SERVER_PORT = 25678;
    private final Socket socket;
    private final ActorRef admin;

    public ServerConnector(ActorRef admin) throws IOException {
        this.admin = admin;
        socket = new Socket(SERVER_ADRESS, SERVER_PORT);
        SocketListener listener = new SocketListener(socket);
        listener.start();
    }

    private class SocketListener extends Thread {
        private final Socket socket;
        private boolean active = true;

        public SocketListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Crawler connected and listening...");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String line;
                while (active) {
                    line = reader.readLine();

                    /* Analyse the input */
                    switch (line) {
                        case "ACTIVATE":
                            System.out.println("SERVER: ACTIVE");
                            admin.tell(new MessageServer("http://www.jsoup.org", false), null);
                            break;
                        case "REFRESH":
                            System.out.println("SERVER: REFRESH");
                            admin.tell(new MessageServer("", true), null);
                            break;
                        default:
                            System.out.println("SERVER INPUT: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
