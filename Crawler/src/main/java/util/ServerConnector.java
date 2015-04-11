package util;

import akka.actor.ActorRef;
import message.MessageActive;
import message.MessageServer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

    private final static String SERVER_ADRESS = "192.168.1.83";
//    private final static String SERVER_ADRESS = "178.21.117.113";
//    private final static String SERVER_ADRESS = "localhost";
    private final static int SERVER_PORT = 25678;
    private Socket socket;
    private ActorRef admin;
    private PrintWriter out;
    private SocketListener listener;

    private String crawlerName = "unknown";

    private final static int
            ACTIVE_URL = 2,
            ACTIVE_SEARCHID = 1,
            ACTIVE_TAG = 3;


    public ServerConnector(ActorRef admin, final UICallables uiCallables) {
        LOGGER.info("Establishing connection with server...");

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            crawlerName = addr.getHostName() + (int)(Math.random()*99);
        } catch (UnknownHostException ex)
        {
            LOGGER.warning("Hostname can not be resolved");
        }

        try {
            this.admin = admin;
            socket = new Socket(SERVER_ADRESS, SERVER_PORT);
            listener = new SocketListener(socket);
            listener.start();
            out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
            out.println("checkin " + crawlerName);
            out.flush();
            uiCallables.updateConnectionStatus(("connected"));
        } catch (IOException e) {
            LOGGER.warning("Connection with server NOT successful");
            uiCallables.updateConnectionStatus(("failed"));
        }
    }

    /**
     * Send a message to the server
     *
     * @param message The message to be send
     */
    public void tellServer(String message) {
        if (socket != null && socket.isConnected()) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Stops the {@link util.ServerConnector.SocketListener listener} and closes the socket.
     *
     * @throws IOException
     */
    public void stopConnection() throws IOException {
        if (socket != null) {
            try {
                listener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            socket.close();
            LOGGER.info("Socket closed");
        }
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
                    LOGGER.warning("Server input: " + Arrays.toString(args));

                    /* Analyse the input */
                    switch (args[0]) {
                        case "activecrawl":
                            LOGGER.info("SERVER: ACTIVE");
                            admin.tell(new MessageActive(args[ACTIVE_URL], 3, Integer.parseInt(args[ACTIVE_SEARCHID]),
                                    args[ACTIVE_TAG]), null);
//                            admin.tell(new MessageServer(args[2], false), null);
                            break;
                        case "updatedatabase":
                            LOGGER.info("SERVER: REFRESH");
                            admin.tell(new MessageServer("", true), null);
                            break;
                        case "shutdown":
                            active = false;
                            break;
                        default:
                            //LOGGER.info("SERVER INPUT: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
