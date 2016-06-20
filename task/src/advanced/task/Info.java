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
    static final String configTag = "config";

    static final String serverTag = "server";
    static final String clientTag = "client";

    static final String portTag = "port";

    static final String hostTag = "ServerHost";

    // arguments number in command line
    static final int cmdLineArgsNumber = 3;
    // correct keys number in command line
    static final int cmdLineKeysNumber = 2;
}
