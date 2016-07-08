package advanced.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static advanced.task.Info.USER_CMD;
import static advanced.task.Info.QUIT_CMD;

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
    private int cmdNumber; // commands number for transmittion

    private String hostName;  // server's host name
    private int portNumber;   // server's port number

    private List<String> recCmdList = new ArrayList<>();

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Client.class.getName());

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

    int compareCmd() {
        int errorsNumber = 0;
        String cmd;
        for (int i = 1; i <= cmdNumber; ++i) {
            cmd = cmdList.get(i);
            if(!recCmdList.get(i).equals(cmd.substring(0, cmd.length() - 1))) {
                ++errorsNumber;
            }
        }

        return errorsNumber;
    }
}


/**
 * Server testing with multiple clients.
 *
 * @author Sergey Sokhnyshev
 * Created on 06.07.16.
 */
public class Testing {
    public static void main(String[] args) {
        // starting server

        new Thread(new Runnable() {

            @Override
            public void run() {
                ConfigReader cfgReader = new ConfigReader("../files/config.xml",
                                                          true);
                new Server(cfgReader.getPortNumber(), new EchoServerListener());
            }
        }, "TestServerThread").start();

        // creating cyclic barrier
        int usrNumber = 100;  // clients number in test
        int cmdNumber = 1000;  // commands number sending by each client
                                 // to server

        // creating and executing clients
        ConfigReader cfgReader = new ConfigReader("../files/config.xml", false);
        List<ClientThread> client = new ArrayList<>(usrNumber);

        CyclicBarrier cb = new CyclicBarrier(usrNumber, new Runnable() {
            @Override
            public void run() {
                System.out.println("All threads are over!");

                int currentErrorsNumber;
                int sumErrorsNumber = 0;

                for(int i = 0; i < usrNumber; ++i) {
                    currentErrorsNumber = client.get(i).compareCmd();
                    sumErrorsNumber += currentErrorsNumber;

                    if (currentErrorsNumber != 0) {
                        System.out.println("User ID: " + i + " errors: " + currentErrorsNumber);
                    }
                }

                if (sumErrorsNumber == 0) {
                    System.out.println("All commands received correctly!");
                }

                System.exit(0);
            }
        });

        for (int i = 0; i < usrNumber; ++i) {
            client.add(new ClientThread(cmdNumber, cfgReader.getHostName(),
                                      cfgReader.getPortNumber(), cb));
        }

/*
        // filling buffer with commands
        List<String> cmd = new LinkedList<String>();
        int commandsNumber = 100;

        // adding name
        cmd.add("Jane" + '\n');

        Random rnd = new Random();

        for (int i = 0; i < commandsNumber; ++i) {
            cmd.add(USER_CMD.get(rnd.nextInt(USER_CMD.size())) + '\n');
        }

        // adding quit command
        cmd.add(QUIT_CMD + '\n');

        System.out.println("Size: " + cmd.size());

        for (String s : cmd) {
            System.out.print(s);
        }

        // getting byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        for (String s : cmd) {
            try {
                out.write(s.getBytes());
            } catch (IOException exc) {
                System.out.println("DataOutputStream error");
                System.exit(1);
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // trying to feed client
        ConfigReader cfgReader = new ConfigReader("../files/config.xml", false);

        Client client = new Client(cfgReader.getHostName(),
                cfgReader.getPortNumber(), bais);

        StorageClientListener storage = new StorageClientListener();

        client.go(storage);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException exc) {

        }

        System.out.println("Received CMD: ");
        for(String s : storage.recCmd) {
            System.out.println(s);
        }

        System.out.println("Received size: " + storage.recCmd.size());
        */
    }
}
