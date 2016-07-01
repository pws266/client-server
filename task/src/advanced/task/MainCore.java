package advanced.task;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for client or server execution.
 * The control is performed via application command line. The execution
 * parameters are defined in specified *.xml configuration file.
 *
 * @author Sergey Sokhnyshev
 * Created on 10.06.16.
 */
class MainCore {
    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Client.class.getName());

    // correct command line parameters
    private static final int CMD_LINE_ARGS_NUMBER = 3;  // arguments number
    private static final int CMD_LINE_KEYS_NUMBER = 2;  // correct keys number
    private static final int MAIN_EXIT_CODE = 1;        // exit code

    public static void main(String[] args) {
        // checking arguments number in command line
        try {
            if (args.length != CMD_LINE_ARGS_NUMBER) {
                throw new Exception("Illegal command line arguments number");
            }
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Usage: java -classpath <path to package " +
                                  "folders> advanced.task.MainCore " +
                                  "<mode> -config <path to cfg file" +
                                  "/cfg file name.xml>\nMode could be: " +
                                  "<-server> or <-client>", exc);
            System.exit(MAIN_EXIT_CODE);
        }

        // reading arguments
        String cfgFileName = "";
        boolean isServer = false;
        int correctKeysNumber = 0;

        try {
            for (int i = 0; i < args.length; ++i) {
                if ("-config".equals(args[i])) {
                    cfgFileName = args[++i];
                    ++correctKeysNumber;

                    continue;
                }

                if ("-server".equals(args[i])) {
                    isServer = true;
                    ++correctKeysNumber;
                } else if ("-client".equals(args[i])) {
                    isServer = false;
                    ++correctKeysNumber;
                }
            }

            if (correctKeysNumber != CMD_LINE_KEYS_NUMBER) {
                throw new Exception("Illegal keys in command " +
                        "line. Correct keys number: " + correctKeysNumber);
            }
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Wrong arguments command line format", exc);
            System.exit(MAIN_EXIT_CODE);
        }

        System.out.println("Server: " + isServer);
        System.out.println("Config: " + cfgFileName);

        // reading specified *.xml - configuration file
        ConfigReader cfgReader = new ConfigReader(cfgFileName, isServer);

        // starting server
        if (isServer) {
            Server server = new Server(cfgReader.getPortNumber());
        }
        // or client
        else {
            Client client = new Client(cfgReader.getHostName(),
                                       cfgReader.getPortNumber(), System.in);
            client.go(new ClientResponder());
        }
    }
}
