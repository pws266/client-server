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
    private int portNumber;  // server port number
    private String hostName; // host name

    private Socket socket;   // socket for connection with server

    private BufferedReader in; // client input stream control
    private PrintWriter out;   // client output stream control

    private BufferedReader cmdIn; // client commands input stream

    private ReplyProcessor srvReply; // client response (with callback method)
                                     // on received server message

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
        this.portNumber = portNumber;
        this.hostName = hostName;

        // initializing client commands input as standard input(by default)
        cmdIn = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Assigns processing method for received server messages
     *
     * @param srvReply - class instance implementing interface "ReplyProcessor"
     */
    public void setReplyProcessor(ReplyProcessor srvReply) {
        this.srvReply = srvReply;
    }

    /**
     * Initiates messages exchange between client and server
     */
    public void startExchange() {
        try {
            // asking user name
            System.out.print("Enter your name, plz: ");
            String userName = cmdIn.readLine();

            // initializing socket for connection with server
            socket = new Socket(hostName, portNumber);

            // initializing input and output client streams
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // sending user name to server
            out.println(userName);
        } catch (IOException exc) {
            System.out.println("Client error: Something wrong with socket or " +
                    "I/O streams. Is server switched on?");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
//        finally {
//            close();
//        }

        String srvMsg;  // message from server
        String usrMsg;  // command from client

        try {
            while ((srvMsg = in.readLine()) != null) {
                System.out.println(srvReply.onProcess(srvMsg));

                System.out.print("> ");
                usrMsg = cmdIn.readLine();

                if (usrMsg != null) {
                    System.out.println("Client: " + usrMsg);
                    out.println(usrMsg);

                    if("quit".equals(usrMsg)) {
                        System.out.println("--- End of connection. Bye! ---");
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

    /**
     * @param inputStream - input commands source. Could be instance of
     *                      "System.in" for standard keyboard input or
     *                      "ByteArrayInputStream" for testing
     */
    public void setInputStream(InputStream inputStream) {
        try {
            cmdIn.close();
        } catch (IOException exc) {
            System.out.println("Client error: Problems with input stream " +
                               "closing");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
//        finally {
//            close();
//        }

        cmdIn = new BufferedReader(new InputStreamReader(inputStream));
    }

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
            System.out.println("Client error: Unable to close socket or " +
                               "I/O streams");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
    }

    public static void main(String[] args) {
        ConfigReader cfgReader = new ConfigReader("../files/config.xml", false);

        Client client = new Client(cfgReader.getHostName(),
                cfgReader.getPortNumber());
        client.setReplyProcessor(new ReplyOnServerMsg());
        client.startExchange();
    }
}
