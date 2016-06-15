package advanced.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server implementation for messages exchange with multiple users
 * Each "client-server" connection is processed in separate thread
 *
 * @author Sergey Sokhnyshev
 * Created on 09.06.16.
 */
public class Server {
    private ServerSocket srvSocket;
    // connections list maintaining by server
    private final List<Connection> connectList;

    private ReplyProcessor usrReply;

    /**
     * Constructor for server instance creation listening specified port
     *
     * @param portNumber - port number listening by server for client connection
     *                     Could be taken from *.xml configuration file
     */
   public Server(int portNumber) {
       connectList = Collections.synchronizedList(new ArrayList<Connection>());

       try {
           srvSocket = new ServerSocket(portNumber);

           while (true) {
               Socket usr_socket = srvSocket.accept();

               // creating connection and adding it to connection list
               Connection link = new Connection(usr_socket);
               connectList.add(link);

               // executing connection in separate thread
               link.start();
           }

       } catch (IOException exc) {
           System.out.println("Server error: Something wrong while /" +
                              "listening on port " + portNumber);
           System.out.println("Error description: " + exc.getMessage());

           //TODO: add logging
       } finally {
           disconnect();
       }
   }

    /**
     * Closes server socket and all maintaining connections
     */
    private void disconnect() {
        try {
            srvSocket.close();

            // carefully closing each connection in list
            // locking list of another threads access for connections closing
            synchronized (connectList) {
                connectList.forEach(Connection::close);
            }
        } catch (IOException exc) {
            System.out.println("Server error: Socket isn't closed");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
    }

    /**
     * Connection implementation of single user
     * @author Sergey Sokhnyshev
     */
    private class Connection extends Thread {
        // socket for client connection created and listed by server
        private Socket socket;

        // server output stream for messages corresponding this connection
        private PrintWriter out;
        // server input stream for messages from cliend served by this connection
        private BufferedReader in;

        // client name corresponding to this connection
        private String userName = "";

        /**
         * Constructor for connection instance initialization and entering
         * client name corresponding to this connection
         *
         * @param socket - socket instance obtained by invoking
         *                 "server.accept()"
         */
        public Connection(Socket socket) {
            this.socket = socket;

            try {

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                                        socket.getInputStream()));
            } catch (IOException exc) {
                System.out.println("Connection error: Unable to initialize /" +
                                   "I/O streams");
                System.out.println("Error description: " + exc.getMessage());

                //TODO: add logging
            }
        }

        /**
         * Body of messages exchange mechanism between client and server
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            String usrMsg;
            String svrMsg;

            try {
                //getting client name
//                do {
//                } while((userName = in.readLine()) == null);
                userName = in.readLine();

                System.out.println("Hello, " + userName +
                           "! You are successfully connected to server!");
                out.println("You are successfully connected to server!");

                while ((usrMsg = in.readLine()) != null) {
                    out.println(usrMsg);
                }


                //TODO: add protocol
                //TODO: print about user entering on client and server side


            } catch (IOException exc) {
                System.out.println("Connection error: I/O error in messages " +
                                   "exchange");
                System.out.println("Error description: " + exc.getMessage());

                //TODO: add logging

            } finally {
                close();
            }
        }

        /**
         * Closes socket and I/O streams corresponding to given connection
         * instance
         */
        public void close() {
            try {
                socket.close();

                in.close();
                out.close();

                // removing current connection from list
                // switching server off if list is empty
                connectList.remove(this);
                if (connectList.isEmpty()) {
                    Server.this.disconnect();
                    System.exit(0);
                }
            } catch (IOException exc) {
                System.out.println("Connection error: Unable to close socket " +
                                   "or I/O streams");
                System.out.println("Error description: " + exc.getMessage());
                //TODO: add logging
            }
        }
    }
}
