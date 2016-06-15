package advanced.task;

import java.io.*;
import java.net.Socket;

/**
 * Client class for communication with server upon text commands
 *
 * @author Sergey Sokhnyshev
 * Created on 09.06.16.
 */
public class Client {
    // socket for connection with server
    private Socket socket;

    // client input stream control
    private BufferedReader in;
    // client output stream control
    private PrintWriter out;

    // client commands input stream
    private BufferedReader cmdIn;
    // client response on received server message
    private ReplyProcessor srvReply;

    /**
     * Constructor creates client instance specifying parameters for
     * connection with server
     *
     * @param hostName - host name of server placement
     * @param portNumber - port number on which server is available for
     *                     connection
     *
     * All parameters could be get via XML configuration file parsing
     */
    public Client(String hostName, int portNumber) {
        try {
            //initializing socket for connection with server
            socket = new Socket(hostName, portNumber);

            //initializing input and output client streams
            in = new BufferedReader(new InputStreamReader(
                                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            //initializing client commands input as standard input(by default)
            cmdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException exc) {
            System.out.println("Client error: Unable to close socket or " +
                               "I/O streams");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        } finally {
            close();
        }
    }

    /**
     * Assigns processing method for received server messages
     *
     * @param srvReply - class instance implementing interface "ReplyProcessor"
     */
    public void assignReplyProcessor(ReplyProcessor srvReply) {
        this.srvReply = srvReply;
    }

    /**
     * Initiates messages exchange between client and server
     */
    public void startExchange() {
        String srvMsg;
        String usrMsg;

        try {
            while ((srvMsg = in.readLine()) != null) {
                srvReply.onProcess(srvMsg);

                usrMsg = cmdIn.readLine();
                if (usrMsg != null) {
                    System.out.println("Client: " + usrMsg);
                    out.println(usrMsg);

                    if("quit".equals(usrMsg)) {
                        break;
                    }
                }
            }
        } catch (IOException exc) {
            System.out.println("Client error: Unable to close socket or " +
                    "I/O streams");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging

        }
    }

    //TODO: add method for ByteArrayInputStream assigning as "cmdIn"

    /**
     * Closes client socket and I/O streams
     */

    private void close() {
        try {
            in.close();
            out.close();

            cmdIn.close();

            socket.close();
        } catch (IOException exc) {
            System.out.println("Client error: Unable to close socket or /" +
                               "I/O streams");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
    }
}
