package advanced.task;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static advanced.task.Info.LOG_RESOURCE_FILE_PATH;
import static advanced.task.Info.MAIN_ANNOTATION;

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
    private static final Logger log = Logger.getLogger(MainCore.class.getName());

    // correct command line parameters
    private static final int CMD_LINE_ARGS_NUMBER = 3;  // arguments number
    private static final int CMD_LINE_KEYS_NUMBER = 2;  // correct keys number
    private static final int MAIN_EXIT_CODE = 1;        // exit code

    /**
     * Attempts to create folder for *.log - files
     * @param logTraits - logging properties loaded as resource
     */
    static void createLoggingFolder(Properties logTraits, Logger log) {
        try {
            String logPattern = logTraits.getProperty(
                    "java.util.logging.FileHandler.pattern");
            if (logPattern != null) {
                String logPath = new File(logPattern).getParent();

                File logFolder = new File(logPath);

                if (!logFolder.exists() && !logFolder.mkdirs()) {
                    throw new Exception("Unable to create folder for " +
                            "*.log files specified in " +
                            "resources");
                }
            }
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Error: no log folder was created: ", exc);
            System.exit(MAIN_EXIT_CODE);
        }
    }

    /**
     * Switches on logging
     */
    private static void enableLogging() {
        // switching on logging
        // resource file should be in the same folder with package folders
        // Using "absolute" path
        try {
            LogManager.getLogManager().readConfiguration(
                    advanced.task.MainCore.class.getResourceAsStream(
                            LOG_RESOURCE_FILE_PATH));
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Error: unable to read logging " +
                    "configuration file", exc);
            System.exit(MAIN_EXIT_CODE);
        }

        // creating folder for logging
        if (log.getParent().getLevel() != Level.OFF) {
            try {
                //getting name of log folder
                Properties logTraits = new Properties();
                logTraits.load(advanced.task.MainCore.class.getResourceAsStream(
                        LOG_RESOURCE_FILE_PATH));

                // attempting to create folder for logs
                createLoggingFolder(logTraits, log);
            } catch (IOException exc){
                log.log(Level.SEVERE, "Error: unable to read/load logging " +
                        "configuration file", exc);
                System.exit(MAIN_EXIT_CODE);
            }
        }
    }

    /**
     * Verifies arguments number in command line
     * @param argNumber - arguments number in command line
     * @param correctArgNumber - correct arguments number in command line
     * @param annotation - brief usage program description with arguments notice
     * @param exitCode - value of exit code
     */
    static void checkCmdLineArgNumber(int argNumber, int correctArgNumber,
                               Logger log, String annotation, int exitCode) {
        try {
            if (argNumber != correctArgNumber) {
                throw new Exception("Illegal command line arguments number");
            }
        } catch (Exception exc) {
            log.log(Level.SEVERE, annotation, exc);
            System.exit(exitCode);
        }
    }


    public static void main(String[] args) {
        // enabling logging
        enableLogging();

        // checking arguments number in command line
        checkCmdLineArgNumber(args.length, CMD_LINE_ARGS_NUMBER, log,
                              MAIN_ANNOTATION, MAIN_EXIT_CODE);

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
            new Server(cfgReader.getPortNumber(), new AIServerListener());
        }
        // or client
        else {
            Client client = new Client(cfgReader.getHostName(),
                                       cfgReader.getPortNumber(), System.in);
            client.go(new SimpleClientListener());
        }
    }
}
