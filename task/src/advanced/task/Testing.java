package advanced.task;

import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static advanced.task.Info.*;

/**
 * Server testing with multiple clients.
 *
 * @author Sergey Sokhnyshev
 * Created on 06.07.16.
 */

/**
 * Client's listener for multiple clients testing case. Collects commands
 * obtained by server from this client.
 */
class StorageClientListener implements ClientListener {
    private List<String> receivedCmdList = null;

    /**
     * Assigns external storage for echo-commands from server side
     * @param externalCmdList - external list of clients commands receiver by
     *                          server and sent back
     */
    void setReceivedCmdList(List<String> externalCmdList) {
        receivedCmdList = externalCmdList;
    }
    /**
     * Collects command obtained by server from this client to inner list
     * @param msg - client's command received by server and sent back
     * @return server message for client output
     */
    @Override
    public String onProcess(String msg) {
        receivedCmdList.add(msg);
        return "Server: " + msg;
    }
}

/**
 * Server listener for forwarding back received client's commands
 */
class EchoServerListener implements ServerListener {
    /**
     * Generates server's response on received client message
     *
     * @param msg - received client message
     * @param connection - reference to connection with client
     *
     * @return server response message
     */
    @Override
    public String onProcess(String msg, Server.Connection connection) {
        return msg;
    }
}

/**
 * Client wrapper allowing execution in separate thread for testing purposes.
 * Generates command list and automatically send its.
 */
class ClientThread implements Runnable {
    // different sizes of sent and received command lists
    static final int SZ_MISMATCH = -1;
    // set of possible client names
    private static int clientCounter = 0;  // clients counter
    private static final String THREAD_NAME = "ClientThread";
    private static final int CLIENT_THREAD_EXIT_CODE = 7;  // exit code
    private static final List<String> USER_NAME = Collections.synchronizedList(
            Arrays.asList("Bob", "Alice", "Jane", "Maga", "Paul", "George",
                          "Fergie", "Nicole", "Alex", "Britney", "Doro"));

    // commands set for sending
    private final List<String> cmdList = new ArrayList<>();
    private final CyclicBarrier cb;      // cyclic barrier for further commands
                                         // statistic computation
    private int clientID;  // client thread unique number
    private int cmdNumber; // commands number for transmission

    private String hostName;  // server's host name
    private int portNumber;   // server's port number

    private List<String> recCmdList = new ArrayList<>();

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(
                                             ClientThread.class.getName());

    /**
     * Creates commands list for transmitting by client to server
     */
    private void generateCmdList() {
        Random rnd = new Random();

        // adding client's name from list

        cmdList.add(USER_NAME.get(rnd.nextInt(USER_NAME.size())) + '\n');

        // adding transmitting commands set
        for (int i = 0; i < cmdNumber; ++i) {
            cmdList.add(USER_CMD.get(rnd.nextInt(USER_CMD.size())) + '\n');
        }

        //adding "quit" command terminating client operation
        cmdList.add(QUIT_CMD + '\n');
    }

    /**
     * Executes client in separate thread automatically transmitting commands to
     * server
     * @param cmdNumber - number of commands transmitting to server by client
     * @param hostName - server's host name
     * @param portNumber - server's port number
     * @param cb - cyclic barrier for signalling of commands statistic
     *             computation start
     */
    ClientThread(int cmdNumber, String hostName, int portNumber,
                 CyclicBarrier cb) {
        // creating client's commands storage: considering commands number,
        // client's name and final "quit" command
        this.cmdNumber = cmdNumber;

        this.hostName = hostName;
        this.portNumber = portNumber;

        this.cb = cb;
        clientID = clientCounter++;

        new Thread(this, THREAD_NAME + clientID).start();
    }

    /**
     * Thread function performing command transmission by client
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // filling commands storage
        generateCmdList();

        // transforming commands to byte representation for automatic
        // transmission by client
        ByteArrayInputStream bais = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos)
        ){
            for (String s : cmdList) {
                out.write(s.getBytes());
            }

            bais = new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException exc) {
            log.log(Level.SEVERE, "ClientThread error: unable to save " +
                    "command for input stream ", exc);
            System.exit(CLIENT_THREAD_EXIT_CODE);
        }

        // creating and executing client
        Client client = new Client(hostName, portNumber, bais);

        StorageClientListener storage = new StorageClientListener();
        storage.setReceivedCmdList(recCmdList);

        client.go(storage);

        try {
            cb.await();
        } catch (InterruptedException exc) {
            log.log(Level.SEVERE, "ClientThread error: thread is interrupted " +
                    "for client ID = " + clientID, exc);
        } catch (BrokenBarrierException exc) {
            log.log(Level.SEVERE, "ClientThread error: cyclic barrier is " +
                    "in broken state for client ID = " + clientID, exc);
        }
    }

    /**
     * Performs received and sent commands list comparison for this client
     * @return number of mismatched commands
     */
    final int compareCmd() {
        int errorsNumber = 0;  // comparison errors number
        String cmd;            // current command

        // checking sent and received command list sizes
        if (recCmdList.size() != cmdList.size() - 1) {
            return SZ_MISMATCH;
        }

        // comparing commands excepting user name and last "quit" command
        for (int i = 1; i <= cmdNumber; ++i) {
            cmd = cmdList.get(i);
            if(!recCmdList.get(i).equals(cmd.substring(0, cmd.length() - 1))) {
                ++errorsNumber;
            }
        }

        return errorsNumber;
    }

    /**
     * @return commands number sending by client to server
     */
    final int getCmdNumber() {
        return cmdNumber;
    }
}

/**
 * Server wrapper allowing execution in separate thread for testing purposes.
 * Receives and sends back commands from connected clients.
 */
class ServerThread implements Runnable {
    private final String cfgFileName;   // configuration file name

    /**
     * Constructor for server wrapper
     * @param cfgFileName - *.xml configuration file path and file name
     */
    ServerThread(String cfgFileName) {
        this.cfgFileName = cfgFileName;
    }

    /**
     * Thread function performing server execution
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        ConfigReader cfgReader = new ConfigReader(cfgFileName, true);
        new Server(cfgReader.getPortNumber(), new EchoServerListener());
    }
}
/**
 * Commands mismatch computer for each client connected to server.
 * Executes by cyclic barrier.
 */
class ErrorComputer implements Runnable {
    private final List<ClientThread> client;  // set of client threads

    /**
     * Constructor of errors computer
     * @param client - client thread list for errors computation
     */
    ErrorComputer(List<ClientThread> client) {
        this.client = client;
    }

    /**
     * Thread function performing number of commands mismatch computation
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        System.out.println("All client commands are processed!");
        System.out.println("\nClients number: " + client.size());
        System.out.println("Commands per client: " +
                   (client.isEmpty() ? 0 : client.get(0).getCmdNumber())
                   + '\n');

        // performing errors computation per each client
        int currentErrorsNumber;  // comparison errors number per client
        int sumErrorsNumber = 0;  // total errors number per test case

        for(int i = 0; i < client.size(); ++i) {
            currentErrorsNumber = client.get(i).compareCmd();
            sumErrorsNumber += currentErrorsNumber;

            if (currentErrorsNumber != 0) {
                if (currentErrorsNumber == ClientThread.SZ_MISMATCH) {
                    System.out.println("User ID: " + i +
                                       " command list sizes mismatch");
                }
                else {
                    System.out.println("User ID: " + i + " errors: " +
                            currentErrorsNumber);
                }
            }
        }

        if (sumErrorsNumber == 0) {
            System.out.println("All commands are received correctly!");
        }

        System.exit(0);
    }
}

/**
 * Testing class for server with multiple clients connection cass.
 * Determines whether all command from all clients are received correctly.
 * The commands are sent from client side to server started in echo mode.
 * The received commands are compared on client side with sent commands,
 */
public class Testing {
    private static final int CMD_LINE_ARGS_NUMBER = 6;  // arguments number
    private static final int CMD_LINE_KEYS_NUMBER = 3;  // correct keys number
    private static final int TESTING_EXIT_CODE = 8;     // exit code

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Testing.class.getName());

    /**
     * Switches on logging and attempts to create folder for *.log - files
     */
    private static void enableLogging() {
        // switching on logging
        // resource file should be in the same folder with package folders
        // Using "absolute" path
        try {
            LogManager.getLogManager().readConfiguration(
                    advanced.task.Testing.class.getResourceAsStream(
                            LOG_RESOURCE_FILE_PATH));
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Error: unable to read logging " +
                    "configuration file", exc);
            System.exit(TESTING_EXIT_CODE);
        }

        // creating folder for logging
        if (log.getParent().getLevel() != Level.OFF) {
            try {
                //getting name of log folder
                Properties logTraits = new Properties();
                logTraits.load(advanced.task.Testing.class.getResourceAsStream(
                        LOG_RESOURCE_FILE_PATH));

                // attempting to create folder for logs
                MainCore.createLoggingFolder(logTraits, log);
            } catch (IOException exc){
                log.log(Level.SEVERE, "Error: unable to read/load logging " +
                        "configuration file", exc);
                System.exit(TESTING_EXIT_CODE);
            }
        }
    }

    public static void main(String[] args) {
        // enabling logging
        enableLogging();

        // checking arguments number in command line
        MainCore.checkCmdLineArgNumber(args.length, CMD_LINE_ARGS_NUMBER, log,
                                       TESTING_ANNOTATION, TESTING_EXIT_CODE);
        // reading arguments
        String cfgFileName = "";  //configuration file name
        int usrNumber = 10;       // clients number in test
        int cmdNumber = 100;       // commands number sending by each client
                                  // to server
        int correctKeysNumber = 0;

        try {
            for (int i = 0; i < args.length; ++i) {
                if ("-config".equals(args[i])) {
                    cfgFileName = args[++i];
                    ++correctKeysNumber;

                    continue;
                }

                if ("-usr".equals(args[i])) {
                    usrNumber = Integer.parseInt(args[++i]);
                    ++correctKeysNumber;

                    continue;
                }

                if ("-cmd".equals(args[i])) {
                    cmdNumber = Integer.parseInt(args[++i]);
                    ++correctKeysNumber;
                }
            }

            if (correctKeysNumber != CMD_LINE_KEYS_NUMBER) {
                throw new Exception("Illegal keys in command " +
                        "line. Correct keys number: " + correctKeysNumber);
            }
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Wrong arguments command line format", exc);
            System.exit(TESTING_EXIT_CODE);
        }

        // starting server
        new Thread(new ServerThread(cfgFileName), "TestServerThread").start();

        // creating and executing clients
        ConfigReader cfgReader = new ConfigReader(cfgFileName, false);
        List<ClientThread> client = new ArrayList<>(usrNumber);

        // creating cyclic barrier: declaring error computation invoking AFTER
        // client's threads finalization
        CyclicBarrier cb = new CyclicBarrier(usrNumber, new ErrorComputer(
                                                            client));

        // starting clients threads for connection to server and sending
        // random set of commands
        for (int i = 0; i < usrNumber; ++i) {
            client.add(new ClientThread(cmdNumber, cfgReader.getHostName(),
                                      cfgReader.getPortNumber(), cb));
        }
    }
}
