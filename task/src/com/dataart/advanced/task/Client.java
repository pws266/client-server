package com.dataart.advanced.task;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dataart.advanced.task.Info.DEFAULT_SZ;

/**
 * Client class for communication with server upon text commands
 *
 * @author Sergey Sokhnyshev
 * Created on 09.06.16.
 */
public class Client {
    private final int portNumber;        // server port number
    private final String hostName;       // host name

    private InputStream inStream;    // external stream for commands input
    private OutputStream outStream;  // external stream for server responses output

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Client.class.getName());

    /**
     * Sends user name to server
     * @param userName - user name obtained from keyboard
     * @param out - output stream connected to client's socket
     * @throws IOException - if some problems occur while messages are written to output stream
     */
    private void sendUserName(String userName, ObjectOutputStream out) throws IOException {
        MessageTraits sentMsg = new MessageTraits();
        sentMsg.setMessage(userName);

        sentMsg.send(out);
    }

    /**
     *
     * @param listener - processes server messages according to predefined method
     * @param receivedMsg - contains received message from server side
     * @throws IOException - throws if some problems occur on writing processed server message to output stream
     */
    private void outputServerProcessedMessage(ClientListener listener, MessageTraits receivedMsg) throws IOException {
        outStream.write(listener.onProcess(receivedMsg.getMessage()).getBytes());
        outStream.write('\n');
    }

    /**
     * Client's side business logic of messages exchange between client and server
     * @param listener - processes server messages according to predefined method
     * @param cmdIn - input stream for cl
     * @param in - input stream connected to client's socket
     * @param out - output stream connected to client's socket
     * @throws IOException - if some problems occur in messages I/O operations with appropriate streams or while
     *                       commands are read from keyboard
     */
    private void exchangeCore(ClientListener listener, BufferedReader cmdIn,
                              ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String usrMsg = "";  // command from client

        MessageTraits sentMsg = new MessageTraits();
        MessageTraits recMsg = new MessageTraits();

        while (recMsg.receive(in) != DEFAULT_SZ && !Info.QUIT_CMD.equals(usrMsg)) {
            outputServerProcessedMessage(listener, recMsg);

            System.out.print("> ");
            usrMsg = cmdIn.readLine();

            if (usrMsg != null) {
                System.out.println("Client: " + usrMsg);

                sentMsg.setClientID(recMsg.getClientID());
                sentMsg.setMessage(usrMsg);

                sentMsg.send(out);
            }
        }

        // typing farewell message if "quit" command is entered
        if (Info.QUIT_CMD.equals(usrMsg)) {
            outputServerProcessedMessage(listener, recMsg);        }
    }

    /**
     * Constructor creates client instance specifying parameters for
     * connection with server
     *
     * @param hostName - host name of server placement
     * @param portNumber - port number on which server is available for connection
     *
     * By default input commands stream is assigned to "System.in" and output commands stream is assigned to
     * "System.out"
     *
     * Constructor parameters could be get via XML configuration file parsing
     */

    public Client(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;

        this.inStream = System.in;
        this.outStream = System.out;
    }

    /**
     * Constructor creates client instance specifying parameters for
     * connection with server
     *
     * @param hostName - host name of server placement
     * @param portNumber - port number on which server is available for connection
     * @param inStream - external stream for clients commands input
     * @param outStream - external stream for responses received from server side
     *
     * Two first constructor parameters could be get via XML configuration file parsing
     */
    public Client(String hostName, int portNumber, InputStream inStream, OutputStream outStream) {
        this(hostName, portNumber);

        setInputStream(inStream);
        setOutputStream(outStream);
    }

    /**
     * Assigns external stream for feeding client commands. Should be invoked before "start" method
     * @param inStream - external stream for feeding client commands set to class instance
     */
    public void setInputStream(InputStream inStream) {
        this.inStream = inStream;
    }

    /**
     * Assigns external stream for server responses output. Should be invoked before "start" method
     * @param outStream - external stream for server responses output
     */
    public void setOutputStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    /**
     * Initiates messages exchange between client and server
     *
     * @param listener - processes server messages according to predefined
     *                   method
     */
    public void start(ClientListener listener) {
        try ( BufferedReader cmdIn = new BufferedReader(new InputStreamReader(inStream)) ) {
            // asking user name
            System.out.print("Enter your name, plz: ");
            String userName = cmdIn.readLine();

            boolean isMessageExchangeStarted = false;

            try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
            ){
                isMessageExchangeStarted = true;

                // sending user name to server
                sendUserName(userName, out);

                // starting commands exchange between client and server
                exchangeCore(listener, cmdIn, in, out);
            } catch (UnknownHostException exc) {
                log.log(Level.SEVERE, "Client \"" + userName + "\" : Unkown error while connecting to host = \"" +
                        hostName + "\" port = " + portNumber, exc);
            } catch (IOException exc) {
                log.log(Level.SEVERE, "Client \"" + userName + "\" I/O error: " + (isMessageExchangeStarted ? "some " +
                        "problems in message interaction with appropriate stream under reading/writing operation" :
                        "failed while connecting to host = \"" + hostName + "\" port = " + portNumber), exc);
            }
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Client error: I/O error in attempt to use external input stream for entering " +
                    "commands", exc);
        }
    }

    public static void main(String[] args) {
        try {
            ConfigReader cfgReader = new ConfigReader();
            cfgReader.parse("../files/config.xml", false);

            Client client = new Client(cfgReader.getHostName(), cfgReader.getPortNumber(), System.in, System.out);
            client.start(new SimpleClientListener());
        } catch(ParserConfigurationException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to get DOM document instance from XML", exc);
        } catch(org.xml.sax.SAXException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to parse given XML content", exc);
        } catch(IOException exc) {
            log.log(Level.SEVERE, "ConfigReader error: some I/O problems occur while parsing XML", exc);
        }
    }
}
