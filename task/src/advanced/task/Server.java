package advanced.task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static advanced.task.CommandTraits.DEFAULT_ID;
import static advanced.task.Info.QUIT_CMD;

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

    // client commands processor (operates via callback)
    private final ServerListener listener;

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Server.class.getName());

    /**
     * Constructor for server instance creation listening specified port
     *
     * @param portNumber - port number listening by server for client connection
     *                     Could be taken from *.xml configuration file
     * @param listener - processes client messages according to predefined
     *                   method
     */
   public Server(int portNumber, ServerListener listener) {
       this.listener = listener;
       connectList = Collections.synchronizedList(new ArrayList<Connection>());

       try (ServerSocket srvSocket = new ServerSocket(portNumber)) {
           System.out.println("Server is started: waiting for connection");

           try {
               while (true) {
                   Socket usrSocket = srvSocket.accept();

                   // creating connection and adding it to connection list
                   Connection link = new Connection(usrSocket);
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
        private static final String THREAD_NAME = "ConnectionThread";
        private static final String WELCOME_MSG = "You are successfully " +
                                                  "connected to server!";
        private static final String QUIT_MSG = "User \"%s\" is disconnected\n";
        // socket for client connection created and listed by server
        private Socket socket;

        private String usrName = "";       // client's name
        private int clientID = DEFAULT_ID; // client ID

        // logger for tracing error messages
        private final Logger log = Logger.getLogger(Client.class.getName());

        /**
         * Constructor for connection instance initialization and entering
         * client name corresponding to this connection
         *
         * @param socket - socket instance obtained by invoking
         *                 "server.accept()"
         */
        Connection(Socket socket) {
            super(THREAD_NAME + Server.this.clientsCounter);
            this.socket = socket;

            clientID = Server.this.clientsCounter++;
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
                String svrMsg;  // sending server message

                //getting client name
                CommandTraits recCmd = Command.receive(in);
                usrName = recCmd.msg;

                System.out.println("Hello, " + usrName + "! " + WELCOME_MSG);
                out.write(Command.pack(new CommandTraits(WELCOME_MSG,
                                                         clientID)));
                out.flush();

                // getting and decoding command from client's side
                while (!(recCmd = Command.receive(in)).isEOF) {
                    System.out.println(usrName + ": " + recCmd.msg);
                    if(QUIT_CMD.equals(recCmd.msg)) {
                        System.out.printf(QUIT_MSG, usrName);
                        break;
                    }

                    synchronized (listener) {
                        svrMsg = listener.onProcess(recCmd.msg, this);
                    }

                    out.write(Command.pack(new CommandTraits(svrMsg, clientID)));
                    out.flush();
                }
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
         * @return user name corresponding to this connection
         */
        final String getUsrName() {
            return usrName;
        }

        /**
         * @return total connections number
         */
        final int getConnectionsNumber() {
            return Server.this.connectList.size();
        }

        /**
         * @return index of this connection in general server connections list
         */
        final int getConnectionIndex() {
            return Server.this.connectList.indexOf(this);
        }

        /**
         * @return client's ID assigned by server
         */
        final int getClientID() {
            return clientID;
        }

        /**
         * Closes socket corresponding to given connection instance
         */
        void close() {
            try {
                socket.close();

                // removing current connection from list
                // switching server off if list is empty
                synchronized (connectList) {
                    connectList.remove(this);
                    if (connectList.isEmpty()) {
                        System.out.println("No active connections: " +
                                "waiting for user connections");
                    }
                }
            } catch (IOException exc) {
                log.log(Level.SEVERE, "Connection error: Unable to close " +
                        "socket", exc);
                System.exit(CONNECTION_EXIT_CODE);
            }
        }
    }

    public static void main(String[] args) {
        ConfigReader cfgReader = new ConfigReader("../files/config.xml", true);
        new Server(cfgReader.getPortNumber(), new AIServerListener());
    }
}
