package com.dataart.advanced.task;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dataart.advanced.task.Info.*;

/**
 * Simple multithread server for messages exchange with multiple clients.
 * Each "client-server" connection is processed in separate thread.
 * Server should be executed in separate thread.
 *
 * @author Sergey Sokhnyshev
 * Created on 09.06.16.
 */
public class Server implements Runnable {
    private volatile boolean isStop = false;  // flag notifying of server stop

    private final int portNumber;  // server's port number
    private int clientsCounter = 0;     // clients counter for ID assigning

    // connections list maintaining by server
    private final List<Connection> connectList;

    // lock for access control to "connectList"
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // client commands processor (operates via callback)
    private final ServerListener listener;

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Server.class.getName());

    /**
     * Disables server disconnecting all clients
     */
    public void stop() {
        isStop = true;
    }

    /**
     * Starts server with termination ability by command "stop" entered from
     * keyboard
     * @param portNumber - port number listening by server for client connection
     *                     Could be taken from *.xml configuration file
     * @param listener - processes client messages according to predefined
     *                   method
     */
    public static void start(int portNumber, ServerListener listener) {
        Server srv = new Server(portNumber, listener);
        new Thread(srv, SERVER_THREAD_NAME).start();

        log.info(String.format("Type \"%s\" for server work termination", SERVER_STOP_CMD));

        try ( BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in)) ) {
            String stopCmd;
            while ((stopCmd = cmdIn.readLine()) != null) {
                if (SERVER_STOP_CMD.compareToIgnoreCase(stopCmd) == 0) {
                    break;
                }
            }
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Server error: Problems while waiting server stop command input", exc);
        } finally {
            srv.stop();
        }
    }

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
        this.portNumber = portNumber;

        connectList = Collections.synchronizedList(new ArrayList<Connection>());
    }

    /**
     * Thread function for server execution in separate thread
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try (ServerSocket srvSocket = new ServerSocket(portNumber)) {
            // setting server socket timeout
            srvSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);

            log.info(SERVER_START_MSG + NO_CONNECTION_MSG);

            try {
                while (!isStop) {
                    Socket usrSocket;

                    try {
                        usrSocket = srvSocket.accept();
                    } catch (SocketTimeoutException exc) {
                        // checking server stop flag if timeout is expired
                        continue;
                    }

                    // creating connection and adding it to connection list
                    Connection link = new Connection(usrSocket, clientsCounter);
                    connectList.add(link);

                    ++clientsCounter;

                    // executing connection in separate thread
                    // link.start();
                    new Thread(link, CONNECTION_THREAD_NAME + link.getClientID()).start();
                }
            } finally {
                synchronized (connectList) {
                    //connectList.forEach(Connection::close);
                    connectList.forEach(Connection::stop);
                }
            }
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Server error: Problems while listening on port = " + portNumber, exc);
        }
    }

    /**
     * Single client connection. Performs client messages processing via method
     * specified by server's field "responder"
     */
    class Connection implements Runnable {
        // socket for client connection created and listed by server
        private final Socket socket;

        private String usrName = "";  // client's name
        private final int clientID;   // client ID

        // logger for tracing error messages
        private final Logger log = Logger.getLogger(Client.class.getName());

        /**
         * Constructor for connection instance initialization and entering
         * client name corresponding to this connection
         *
         * @param socket - socket instance obtained by invoking
         *                 "server.accept()"
         */
        Connection(Socket socket, int clientID) {
            this.socket = socket;
            this.clientID = clientID;
        }

        /**
         * Body of messages exchange mechanism between client and server
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                String svrMsg;  // sending server message

                //getting client name
                MessageTraits recMsg = new MessageTraits();
                recMsg.receive(in);

                usrName = recMsg.getMessage();

                log.info(String.format(CONNECTION_GREETENG_MSG + CONNECTION_WELCOME_MSG, usrName));

                MessageTraits sentMsg = new MessageTraits();
                sentMsg.setClientID(clientID);
                sentMsg.setMessage(CONNECTION_WELCOME_MSG);

                sentMsg.send(out);

                // getting and decoding command from client's side
                while (recMsg.receive(in) != DEFAULT_SZ) {
                    log.info(usrName + ": " + recMsg.getMessage());

                    if(QUIT_CMD.equals(recMsg.getMessage())) {
                        log.info(String.format(CONNECTION_QUIT_MSG, usrName));
                        break;
                    }

                    svrMsg = listener.onProcess(recMsg.getMessage(), this);

                    sentMsg.setMessage(svrMsg);
                    sentMsg.send(out);
                }
            } catch (IOException exc) {
                log.log(Level.SEVERE, (usrName.isEmpty() ? "Unestablished connection" : "Connection with user \"" +
                        usrName + "\"") + " error: problems with I/O while messages exchange is proceeded", exc);
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
            readLock.lock();

            try {
                return Server.this.connectList.size();
            } finally {
                readLock.unlock();
            }
        }

        /**
         * @return index of this connection in general server connections list
         */
        final int getConnectionIndex() {
            readLock.lock();

            try {
                return Server.this.connectList.indexOf(this);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * @return client's ID assigned by server
         */
        final int getClientID() {
            return clientID;
        }

        /**
         * Stops and closes given connection
         */
        void stop() {
            try {
                socket.shutdownInput();
            } catch (IOException exc) {
                log.log(Level.SEVERE, "Connection error: unexpected error is occured while shutting down socket " +
                        "input stream", exc);
            }
        }

        /**
         * Closes socket corresponding to given connection instance
         */
        void close() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }

                // removing current connection from list
                // switching server off if list is empty
                writeLock.lock();
                try {
                    connectList.remove(this);
                    if (connectList.isEmpty()) {
                        log.info(NO_CONNECTION_MSG);
                    }
                } finally {
                    writeLock.unlock();
                }
            } catch (IOException exc) {
                log.log(Level.SEVERE, "Connection error: Unable to close socket", exc);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ConfigReader cfgReader = new ConfigReader();
            cfgReader.parse("../files/config.xml", true);

            Server.start(cfgReader.getPortNumber(), new AIServerListener());
        } catch(ParserConfigurationException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to get DOM document instance from XML", exc);
        } catch(org.xml.sax.SAXException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to parse given XML content", exc);
        } catch(IOException exc) {
            log.log(Level.SEVERE, "ConfigReader error: some I/O problems occur while parsing XML", exc);
        }
    }
}
