package advanced.task;

/**
 * Reference information for client-server application.
 * The class stores numeric constants, strings, etc. using in project classes
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
class Info {
    /**
     * Private constructor for preventing class instances creation
     */
    private Info() {
        throw new AssertionError();
    }

    // tags for XML configuration file parsing
    static final String configTag;

    static final String serverTag;
    static final String clientTag;

    static final String portTag;

    static final String hostTag;

    static final int cmdLineArgsNumber;  // arguments number in command line
    static final int cmdLineKeysNumber;  // correct keys number in command line

    // static fields initialization
    static {
        configTag = "config";

        serverTag = "server";
        clientTag = "client";

        portTag = "port";

        hostTag = "ServerHost";

        cmdLineArgsNumber = 3;
        cmdLineKeysNumber = 2;
    }
}
