package advanced.task;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static advanced.task.Info.QUIT_CMD;
import static advanced.task.Info.CLIENT_QUIT_MSG;
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
    public void go(ClientListener listener) {
        try ( BufferedReader cmdIn = new BufferedReader(
                                     new InputStreamReader(inStream)) ) {
            // asking user name
            System.out.print("Enter your name, plz: ");
            String userName = cmdIn.readLine();

            try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectInputStream in = new ObjectInputStream(
                                       socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(
                                         socket.getOutputStream())
            ){
                // sending user name to server
                MessageTraits sentMsg = new MessageTraits();
                sentMsg.setMessage(userName);

                sentMsg.send(out);

                String usrMsg;  // command from client
                MessageTraits recMsg = new MessageTraits();

                while (recMsg.receive(in) != -1) {
                    System.out.println(listener.onProcess(recMsg.getMessage()));

                    System.out.print("> ");
                    usrMsg = cmdIn.readLine();

                    if (usrMsg != null) {
                        System.out.println("Client: " + usrMsg);

                        sentMsg.setClientID(recMsg.getClientID());
                        sentMsg.setMessage(usrMsg);

                        sentMsg.send(out);

                        if (QUIT_CMD.equals(usrMsg)) {
                            System.out.println(CLIENT_QUIT_MSG);
                            break;
                        }
                    }
                }
            } catch (UnknownHostException exc) {
                log.log(Level.SEVERE, "Client \"" + userName + "\" : Unkown " +
                        "error while connecting to host = \"" + hostName +
                        "\" port = " + portNumber, exc);
            } catch (IOException exc) {
                log.log(Level.SEVERE, "Client \"" + userName + "\" : I/O " +
                        "error while connecting to host = \"" + hostName +
                        "\" port = " + portNumber, exc);
            }
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Client error: I/O error in attempt to " +
                    "use external input stream for entering commands", exc);
        }
    }

    public static void main(String[] args) {
        ConfigReader cfgReader = new ConfigReader();
        cfgReader.parse("../files/config.xml", false);

        Client client = new Client(cfgReader.getHostName(),
                                   cfgReader.getPortNumber(), System.in);
        client.go(new SimpleClientListener());
    }
}
