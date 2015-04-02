package util;

import akka.actor.ActorRef;
import message.MessageActive;
import message.MessageServer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

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

    private final static Logger LOGGER = Logger.getLogger(ServerConnector.class.getName());

    private final static String SERVER_ADRESS = "localhost";
    private final static int SERVER_PORT = 25678;
    private Socket socket;
    private ActorRef admin;
    private PrintWriter out;
    private SocketListener listener;

    private final static int
            ACTIVE_URL = 2,
            ACTIVE_SEARCHID = 1,
            ACTIVE_TAG = 3;


    public ServerConnector(ActorRef admin) {
        LOGGER.info("Establishing connection with server...");
        try {
            this.admin = admin;
            socket = new Socket(SERVER_ADRESS, SERVER_PORT);
            listener = new SocketListener(socket);
            listener.start();
            out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
            out.println("checkin testcrawler");
            out.flush();
        } catch (IOException e) {
            LOGGER.warning("Connection with server NOT successful");
        }
    }

    /**
     * Send a message to the server
     *
     * @param message The message to be send
     */
    public void tellServer(String message) {
        out.println(message);
        out.flush();
    }

    /**
     * Stops the {@link util.ServerConnector.SocketListener listener} and closes the socket.
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        listener.interrupt();
        socket.close();
        System.out.println("Socket closed");
    }

    /**
     * A thread that listens to incoming messages from the server.
     */
    private class SocketListener extends Thread {
        private final Socket socket;
        private boolean active = true;

        public SocketListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            LOGGER.info("Crawler connected and listening...");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String line;
                while (active) {
                    line = reader.readLine();

                    String[] args = line.trim().split(" ");
                    LOGGER.info("Server input: " + Arrays.toString(args));

                    /* Analyse the input */
                    switch (args[0]) {
                        case "activecrawl":
                            System.out.println("SERVER: ACTIVE");
                            admin.tell(new MessageActive(args[ACTIVE_URL], 3, Integer.parseInt(args[ACTIVE_SEARCHID]),
                                    args[ACTIVE_TAG]), null);
//                            admin.tell(new MessageServer(args[2], false), null);
                            break;
                        case "updatedatabase":
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
