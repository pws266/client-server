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

    private final InputStream inStream;  // external stream for commands input

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
            System.out.println(listener.onProcess(recMsg.getMessage()));

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
            System.out.println(Info.CLIENT_QUIT_MSG);
        }
    }

    /**
     * Constructor creates client instance specifying parameters for
     * connection with server
     *
     * @param hostName - host name of server placement
     * @param portNumber - port number on which server is available for
     *                     connection
     * @param inStream - external stream for clients commands obtaining
     *
     * Two first constructor parameters could be get via XML configuration
     * file parsing
     */
    public Client(String hostName, int portNumber, InputStream inStream) {
        this.portNumber = portNumber;
        this.hostName = hostName;

        this.inStream = inStream;
    }

    /**
     * Initiates messages exchange between client and server
     *
     * @param listener - processes server messages according to predefined
     *                   method
     */
    public void startExchange(ClientListener listener) {
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

            Client client = new Client(cfgReader.getHostName(), cfgReader.getPortNumber(), System.in);
            client.startExchange(new SimpleClientListener());
        } catch(ParserConfigurationException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to get DOM document instance from XML", exc);
        } catch(org.xml.sax.SAXException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to parse given XML content", exc);
        } catch(IOException exc) {
            log.log(Level.SEVERE, "ConfigReader error: some I/O problems occur while parsing XML", exc);
        }
    }
}
