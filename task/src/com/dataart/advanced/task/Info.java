package com.dataart.advanced.task;

import java.util.*;

/**
 * Reference information for client-server application.
 * The class stores numeric constants, strings, etc. using in project classes
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
public class Info {
    /**
     * Private constructor for preventing class instances creation
     */
    private Info() {
        throw new AssertionError();
    }

    // correct command line parameters
    public static final int CMD_LINE_ARGS_NUMBER = 3;  // arguments number
    public static final int CMD_LINE_KEYS_NUMBER = 2;  // correct keys number

    // separator in *.log - file
    public static final String LOG_SEPARATOR = "----------------------------------------------------------------------";
    // constants for server usage
    // connection timeout for server socket for server stop possibility
    public static final int SERVER_SOCKET_TIMEOUT = 1000;   // in ms
    // keyboard command for server stop
    public static final String SERVER_STOP_CMD = "stop";

    // server thread name
    public static final String SERVER_THREAD_NAME = "MultiClientServerThread";
    // server starting message
    public static final String SERVER_START_MSG = "Server is successfully " +
                                                  "started!\n";
    // constants for connection usage
    // message displaying if no clients are connected to server
    public static final String NO_CONNECTION_MSG = "No active connections. " +
                                                   "Waiting for clients";
    // connection thread name
    public static final String CONNECTION_THREAD_NAME = "ConnectionThread#";

    // message for connected client greetings
    public static final String CONNECTION_GREETENG_MSG = "Hello, %s! ";
    // message sending to client in successful connection case
    public static final String CONNECTION_WELCOME_MSG = "You are " +
                               "successfully connected to server!";
    // message displaying on server in client disconnect case
    public static final String CONNECTION_QUIT_MSG = "User \"%s\" is " +
                                                     "disconnected\n";
    // constants for messages packing/extracting from stream
    public static final int DEFAULT_ID = -1;  // client's ID default value
    public static final int DEFAULT_SZ = -1;  // default command size value
    // service symbols substitutions
    public static final Map<String, String> SYMBOL_SUBSTITUTION =
            new HashMap<String, String>() {{
                put("\\n", "\n");
                put("\\r", "\r");
                put("\\0", "\0");
    }};
    // tags for XML configuration file parsing
    // whole configuration section tag
    public static final String CONFIG_TAG = "config";

    public static final String SERVER_TAG = "server";  // server section tag
    public static final String CLIENT_TAG = "client";  // client section tag

    public static final String PORT_TAG = "port";      // client/server port tag

    public static final String HOST_TAG = "ServerHost"; // server host name tag

    // "absolute" path to logging resource properties file
    public static final String LOG_RESOURCE_FILE_PATH =
                               "/res/logging.properties";

    public static final int CMD_NOT_FOUND = -1;
    // command for client disconnection
    public static final String QUIT_CMD = "quit";
    // default server reply for unknown command token
    public static final AIServerListener.UserCmd DEFAULT_CMD = new AIServerListener().new UserCmd("",
                                         "Unknown command. Should I consider it like a message to a world?", null);

    public static final List<AIServerListener.UserCmd> KNOWN_CMD =
            Collections.synchronizedList(
                    new ArrayList<AIServerListener.UserCmd>() {{
                AIServerListener ail = new AIServerListener();

                // greeting
                add(ail.new UserCmd("hello", "Hi, %s!", ail.new UserNameAction()));
                // ask client's name
                add(ail.new UserCmd("my name", "Hmm... you had introduced as \"%s\"", ail.new UserNameAction()));
                // ask server's name
                add(ail.new UserCmd("your name", "\"You can call me Susan if it makes you happy\"(c)Snatch", null));
                // ask current time
                add(ail.new UserCmd("time", "The current time is: %s", ail.new TimeAction("kk:mm:ss XXX")));
                // ask current date
                add(ail.new UserCmd("date", "Today is: %s", ail.new TimeAction("EEE, MMM dd, yyyy")));
                // swear an oath
                add(ail.new UserCmd("fuck", ":) ... Spielberg. Watch your tongue!", null));
                // ask total connections number
                add(ail.new UserCmd("total", "Total connections number is: %d", ail.new TotalConnectionsAction()));
                // ask connection index
                add(ail.new UserCmd("my number", "Your connection index is: %s", ail.new ConnectionIndexAction()));
                // get maintained user commands set
                add(ail.new UserCmd("help", "The commands could contain these known tokens:\n- ", null));
                // get client's ID assigned by server
                add(ail.new UserCmd("id", "Client's ID is: %d", ail.new ClientIDAction()));
                // quit
                add(ail.new UserCmd(QUIT_CMD, "You are disconnected from server, %s! So long!",
                    ail.new UserNameAction()));

            }});

    // constants for Testing - class
    // different sizes of sent and received command lists
    public static final int SZ_MISMATCH = -1;
    // client's thread name preamble
    public static final String TESTING_CLIENT_THREAD_NAME = "TestClientThread#";
    // user names for testing command vectors generation
    public static final List<String> TESTING_USER_NAME =
            Collections.synchronizedList(Arrays.asList("Bob", "Alice", "Jane",
                    "Maga", "Paul", "George", "Fergie", "Nicole", "Alex",
                    "Gloria", "Marty", "Melman", "Britney", "Doro"));

    // correct testing application command line parameters
    // arguments number
    public static final int TESTING_CMD_LINE_ARGS_NUMBER = 6;
    // correct keys number
    public static final int TESTING_CMD_LINE_KEYS_NUMBER = 3;

    // usage brief annotation for testing class
    public static final String TESTING_ANNOTATION = "Usage: java " +
            "-classpath <path_to_package_folders> Testing\n" +
            "-config <path_to_cfg_file/cfg file name.xml>\n" +
            "-usr <clients_number>\n" +
            "-cmd <commands_number_per_client>";

    // usage brief annotation foe main class
    public static final String MAIN_ANNOTATION = "Usage: java -classpath " +
            "<path to package folders> MainCore\n" +
            "-config <path to cfg file/cfg file name.xml>\n" +
            "<mode> (-client or -server)";

    static {
        // forming response for "help" command
        KNOWN_CMD.stream()
                .filter(t -> "help".equals(t.getToken()))
                .findAny()
                .ifPresent(t -> {
                    StringJoiner sj = new StringJoiner(
                            "\n- ", t.getResponse(), "\n");
                    KNOWN_CMD.forEach(x -> sj.add(x.getToken()));
                    t.setResponse(sj.toString());
                });
    }
}
