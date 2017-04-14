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
public class MainCore {
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
    private static boolean createLoggingFolder(Properties logTraits, Logger log) {
        String logPattern = logTraits.getProperty("java.util.logging.FileHandler.pattern");
        if (logPattern != null) {
            String logPath = new File(logPattern).getParent();

            File logFolder = new File(logPath);

            if (!logFolder.exists() && !logFolder.mkdirs()) {
                log.log(Level.SEVERE, "Error: Unable to create folder for *.log files specified in resources");

                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Switches on logging
     */
    private static boolean enableLogging() {
        // switching on logging, configuring loggers via *.properties files parameters reading
        // resource file should be in the same folder with package folders
        // Using "absolute" path
        try {
            LogManager.getLogManager().readConfiguration(MainCore.class.getResourceAsStream(LOG_RESOURCE_FILE_PATH));
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Error: unable to read logging configuration file", exc);
            return false;
        }

        //getting name of log folder
        Properties logTraits = new Properties();

        // creating folder for logging
        if (log.getParent().getLevel() != Level.OFF) {
            try {
                logTraits.load(MainCore.class.getResourceAsStream(LOG_RESOURCE_FILE_PATH));
            } catch (IOException exc){
                log.log(Level.SEVERE, "Error: unable to read/load logging " +
                        "configuration file", exc);
                return false;
            }

            // attempting to create folder for logs
            return createLoggingFolder(logTraits, log);
        }

        return true;
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
     * Parses command line to appropriate keys and values. Verifies arguments number in command line
     * Performs simple verification of command line content
     * @param args - command line for parsing
     */
    private static boolean parseCommandLine(String[] args) {
        if (args.length != CMD_LINE_ARGS_NUMBER) {
            log.log(Level.SEVERE, "Illegal command line arguments number\n" + MAIN_ANNOTATION);
            return false;
        }

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
            log.log(Level.SEVERE, "Illegal keys in command line. Correct keys number: " + correctKeysNumber);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        // enabling logging
        if (!enableLogging()) {
            return;
        }

        try {
            // parsing command line keys and arguments
            if (!parseCommandLine(args)) {
                return;
            }

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
                        cfgReader.getPortNumber(), System.in, System.out);
                client.start(new SimpleClientListener());
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
        }
    }
}
