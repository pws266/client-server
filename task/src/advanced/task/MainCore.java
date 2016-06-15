package advanced.task;

/**
 * Custom exception class for command line arguments verification
 *
 * @author Sergey Sokhnyshev
 * Created on 10.06.16.
 */

class IncorrectCmdLineException extends Exception {
    private String msg;

    IncorrectCmdLineException(String msg) {
        // using superclass constructor for correct detailed message output
        super(msg);
        this.msg = msg;
    }

    public String toString() {
        return "IncorrectCmdLineException: " + msg;
    }
}

/**
 * Class for client or server execution.
 * The control is performed via application command line. The execution
 * parameters are defined in specified *.xml configuration file.
 *
 * @author Sergey Sokhnyshev
 * Created on 10.06.16.
 */
class MainCore {
    public static void main(String[] args) {
        // checking arguments number in command line
        if (args.length != Info.cmdLineArgsNumber) {
            //TODO: add StringBuilder instead of set of lines
            System.err.println("Illegal command line arguments number");
            System.err.println("Usage: java -classpath <path to package " +
                               "folders> advanced.task.task " +
                               "<mode> -config <path to cfg file" +
                               "/cfg file name.xml>");
            System.err.println("Mode could be: <-server> or <-client>");

            System.exit(1);

            //TODO: add logging
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

            if (correctKeysNumber != Info.cmdLineKeysNumber) {
                throw new IncorrectCmdLineException("Illegal keys in command " +
                        "line. Correct keys number: " + correctKeysNumber);
            }
        } catch (IncorrectCmdLineException exc) {
            System.err.println("Error: " + exc.getMessage());
            System.exit(1);
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
                                       cfgReader.getPortNumber());
            client.startExchange();
        }
    }
}
