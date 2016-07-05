package advanced.task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple multithread server for messages exchange with multiple clients.
 * Each "client-server" connection is processed in separate thread
 *
 * @author Sergey Sokhnyshev
 * Created on 09.06.16.
 */
public class Server {
    private static final int SERVER_EXIT_CODE = 2;  // exit code

    private int clientsCounter = 0;     // clients counter for ID assigning

    // connections list maintaining by server
    private final List<Connection> connectList;

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Client.class.getName());

    /**
     * Constructor for server instance creation listening specified port
     *
     * @param portNumber - port number listening by server for client connection
     *                     Could be taken from *.xml configuration file
     */
   public Server(int portNumber) {
       connectList = Collections.synchronizedList(new ArrayList<Connection>());

       try ( ServerSocket srvSocket = new ServerSocket(portNumber) ) {
           System.out.println("Server is started: waiting for connection");

           try {
               while (true) {
                   Socket usrSocket = srvSocket.accept();

                   // creating connection and adding it to connection list
                   Connection link = new Connection(usrSocket,
                                             new ServerResponder<>());
                   connectList.add(link);

                   // executing connection in separate thread
                   link.start();
               }
           } finally {
               synchronized (connectList) {
                   connectList.forEach(Connection::close);
               }
           }
       } catch (IOException exc) {
           log.log(Level.SEVERE, "Server error: Problems while listening on " +
                   "port = " + portNumber, exc);
           System.exit(SERVER_EXIT_CODE);
       }
   }

    /**
     * Single client connection. Performs client messages processing via method
     * specified by server's field "responder"
     *
     * @author Sergey Sokhnyshev
     */
    class Connection extends Thread {
        private static final int CONNECTION_EXIT_CODE = 4;  // exit code
        // socket for client connection created and listed by server
        private Socket socket;

        // client commands processor (operates via callback)
        private Responder responder;

        // client name corresponding to this connection
        private String usrName = "";

        // logger for tracing error messages
        private final Logger log = Logger.getLogger(Client.class.getName());

        /**
         * Constructor for connection instance initialization and entering
         * client name corresponding to this connection
         *
         * @param socket - socket instance obtained by invoking
         *                 "server.accept()"
         * @param responder - processes client messages according to predefined
         *                    method
         */
        Connection(Socket socket, Responder<Connection> responder) {
            super("SvrThread");
            this.socket = socket;

            this.responder = responder;
            responder.onSetup(this);
        }

        /**
         * Body of messages exchange mechanism between client and server
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try (
                DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()))
            ) {
                String usrMsg;  // received client message
                String svrMsg;  // sending server message

                //getting client name
                CommandTraits recCmd = Command.receive(in);
                usrName = recCmd.msg;

                System.out.println("Hello, " + usrName +
                           "! You are successfully connected to server!");
                out.write(Command.pack(new CommandTraits(
                        "You are successfully connected to server!",
                        Server.this.clientsCounter++)));
                out.flush();
//                out.println("You are successfully connected to server!");

//                while ((usrMsg = in.readLine()) != null) {
                // getting and decoding command from client's side
                while (!(recCmd = Command.receive(in)).isEOF) {
                    // getting and decoding command from client's side
                    System.out.println(usrName + ": " + recCmd.msg);
                    if("quit".equals(recCmd.msg)) {
                        System.out.println("User \"" + usrName +
                                           "\" is disconnected");
                        break;
                    }

                    svrMsg = responder.onProcess(recCmd.msg);

//                    out.println(svrMsg);
                    out.write(Command.pack(new CommandTraits(svrMsg,
                                                             recCmd.clientID)));
                    out.flush();
                }


                //TODO: add protocol
                //TODO: print about user entering on client and server side


            } catch (IOException exc) {
                log.log(Level.SEVERE, (usrName.isEmpty() ?
                        "Unestablished connection" : "Connection with user \"" +
                        usrName + "\"") + " error: problems with I/O while " +
                        "messages exchange is proceeded", exc);
                System.exit(CONNECTION_EXIT_CODE);
            } finally {
                close();
            }
        }

        /**
         * Returns user name corresponding to this connection
         */
        final String getUsrName() {
            return usrName;
        }

        /**
         * Returns total connections number
         */
        final int getConnectionsNumber() {
            return Server.this.connectList.size();
        }

        /**
         * Returns given connection index
         */
        final int getConnectionIndex() {
            return Server.this.connectList.indexOf(this);
        }

        /**
         * Closes socket corresponding to given connection instance
         */
        void close() {
            try {
                socket.close();

                // removing current connection from list
                // switching server off if list is empty
                connectList.remove(this);
                if (connectList.isEmpty()) {
                    System.out.println("No active connections: " +
                                       "waiting for user connections");
                }

            } catch (IOException exc) {
                log.log(Level.SEVERE, "Connection error: Unable to close " +"" +
                        "socket", exc);
                System.exit(CONNECTION_EXIT_CODE);
            }
        }
    }

    public static void main(String[] args) {
        ConfigReader cfgReader = new ConfigReader("../files/config.xml", true);
        new Server(cfgReader.getPortNumber());
    }
}
