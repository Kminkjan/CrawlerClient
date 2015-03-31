package util;

import akka.actor.ActorRef;
import message.MessageServer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

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
    private final PrintWriter out;
    private final SocketListener listener;


    public ServerConnector(ActorRef admin) throws IOException {
        System.out.println("TRYING TO SEND");
        this.admin = admin;
        socket = new Socket(SERVER_ADRESS, SERVER_PORT);
        listener = new SocketListener(socket);
        listener.start();
        out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
        out.println("checkin testcrawler");
        out.flush();

        out.println("searchpoll 1 1 1 1");
        out.flush();

        out.println("searchpoll 1 4 4 4");
        out.flush();
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
            System.out.println("Crawler connected and listening...");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String line;
                while (active) {
                    line = reader.readLine();

                    String[] args = line.trim().split(" ");
                    System.out.println("Server input: " + Arrays.toString(args));

                    /* Analyse the input */
                    switch (args[0]) {
                        case "activecrawl":
                            System.out.println("SERVER: ACTIVE");
                            admin.tell(new MessageServer(args[2], false), null);
                            break;
                        case "updatedaabase":
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
