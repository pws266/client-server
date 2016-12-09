package com.dataart.advanced.task;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.dataart.advanced.task.Info.*;

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

    // *.xml configuration file name
    private static String cfgFileName = "";
    // notification flag for server/client execution
    private static boolean isServer = false;

    /**
     * Attempts to create folder for *.log - files
     * @param logTraits - logging properties loaded as resource
     */
    static void createLoggingFolder(Properties logTraits,
                                    Logger log) {
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
                    MainCore.class.getResourceAsStream(
                            LOG_RESOURCE_FILE_PATH));
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Error: unable to read logging " +
                    "configuration file", exc);
        }

        // creating folder for logging
        if (log.getParent().getLevel() != Level.OFF) {
            try {
                //getting name of log folder
                Properties logTraits = new Properties();
                logTraits.load(MainCore.class.getResourceAsStream(
                        LOG_RESOURCE_FILE_PATH));

                // attempting to create folder for logs
                createLoggingFolder(logTraits, log);
            } catch (IOException exc){
                log.log(Level.SEVERE, "Error: unable to read/load logging " +
                        "configuration file", exc);
            }
        }
    }

    /**
     * Logging information about JRE and OS in
     */
    private static void logSystemInfo() {
        // saving information to log about OS and JRE
        log.info(LOG_SEPARATOR);

        log.info("OS:\n - name: " + System.getProperty("os.name") +
                 "\n - platform: " + System.getProperty("os.arch") +
                 "\n - version: " + System.getProperty("os.version") + "\n");

        log.info("JRE:\n - vendor: " +
                 System.getProperty("java.specification.vendor") +
                 "\n - name: " +
                 System.getProperty("java.specification.name") +
                 "\n - version: " +
                 System.getProperty("java.specification.version") + "\n");

        log.info(LOG_SEPARATOR);
    }

    /**
     * Verifies arguments number in command line
     * @param argNumber - arguments number in command line
     * @param correctArgNumber - correct arguments number in command line
     * @param annotation - brief usage program description with arguments notice
     */
    static void checkCmdLineArgNumber(int argNumber, int correctArgNumber, Logger log, String annotation) throws Exception{
//        try {
            if (argNumber != correctArgNumber) {
                throw new Exception("Illegal command line arguments number\n" + annotation);
            }
//        } catch (Exception exc) {
//            log.log(Level.SEVERE, annotation, exc);
//        }
    }

    /**
     * Parses command line to appropriate keys and values.
     * Performs simple verification of command line content
     * @param args - command line for parsing
     */
    private static void parseCommandLine(String[] args) throws Exception {
        int correctKeysNumber = 0;

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
            throw new Exception("Illegal keys in command line. Correct keys number: " + correctKeysNumber);
        }
    }

    public static void main(String[] args) {
        // enabling logging
        enableLogging();

        try {
            // checking arguments number in command line
            checkCmdLineArgNumber(args.length, CMD_LINE_ARGS_NUMBER, log, MAIN_ANNOTATION);
            // parsing command line keys and arguments
            parseCommandLine(args);

            log.info((isServer ? "Server" : "Client") + " will be started");
            log.info("Configuration file name: " + cfgFileName);

            // reading specified *.xml - configuration file
            ConfigReader cfgReader = new ConfigReader();

            cfgReader.parse(cfgFileName, isServer);

            // starting server
            if (isServer) {
                logSystemInfo();
                Server.start(cfgReader.getPortNumber(), new AIServerListener());
            }
            // or client
            else {
                Client client = new Client(cfgReader.getHostName(),
                        cfgReader.getPortNumber(), System.in);
                client.startExchange(new SimpleClientListener());
            }
        } catch(ParserConfigurationException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to get DOM " +
                    "document instance from XML", exc);
        } catch(org.xml.sax.SAXException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to parse given " +
                    "XML content", exc);
        } catch(IOException exc) {
            log.log(Level.SEVERE, "ConfigReader error: some I/O problems " +
                    "occur while parsing XML", exc);
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Wrong arguments command line format", exc);
        }
    }
}
