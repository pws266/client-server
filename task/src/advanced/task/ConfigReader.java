package advanced.task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration file reader.
 * Parses specified *.xml - file and stores parameters for client and server
 * in appropriate variables
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
class ConfigReader {
    private static final int CONFIG_EXIT_CODE = 5;  // exit code

    // tags for XML configuration file parsing
    private static final String CONFIG_TAG = "config";

    private static final String SERVER_TAG = "server";
    private static final String CLIENT_TAG = "client";

    private static final String PORT_TAG = "port";

    private static final String HOST_TAG = "ServerHost";

    // client/server port number
    private int portNumber = 0;
    // host name for client's usage
    private String hostName;

    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(
                                             ConfigReader.class.getName());

    /**
     * Constructor reads parameters from specified *.xml configuration file
     *
     * @param cfgFileName - path and name of *.xml configuration file
     * @param isServer - defines the appropriate section in configuration
     *                      file corresponding to client or server
     */
    ConfigReader(String cfgFileName, boolean isServer) {
        File cfgFile = new File(cfgFileName);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.
                                             newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(cfgFile);

            doc.getDocumentElement().normalize();

            // parsing XML body
            NodeList cfgList = doc.getElementsByTagName(CONFIG_TAG);

            Node cfgNode = cfgList.item(0);
            if (cfgNode.getNodeType() == Node.ELEMENT_NODE) {
                Element cfgElm = (Element)cfgNode;

                // reading port value for client/server
                String dstTag = isServer ? SERVER_TAG : CLIENT_TAG;
                NodeList dstList = cfgElm.getElementsByTagName(dstTag);

                Node dstNode = dstList.item(0);
                if (dstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dstElm = (Element)dstNode;
                    portNumber = Integer.parseInt(dstElm.getAttribute(
                                                  PORT_TAG));

                    // getting host name for client
                    if (!isServer) {
                        NodeList hostList = dstElm.getElementsByTagName(
                                                   HOST_TAG);
                        Node hostNode = hostList.item(0);
                        if (hostNode.getNodeType() == Node.ELEMENT_NODE) {
                            NodeList list = hostNode.getChildNodes();
                            hostName = list.item(0).getNodeValue();
                        }
                    }
                    else {
                        hostName = null;
                    }
                }
            }

        } catch(ParserConfigurationException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to get DOM " +
                    "document instance from XML", exc);
            System.exit(CONFIG_EXIT_CODE);
        } catch(org.xml.sax.SAXException exc) {
            log.log(Level.SEVERE, "ConfigReader error: unable to parse given " +
                    "XML content", exc);
            System.exit(CONFIG_EXIT_CODE);
        } catch(IOException exc) {
            log.log(Level.SEVERE, "ConfigReader error: some I/O problems " +
                    "occur while parsing XML", exc);
            System.exit(CONFIG_EXIT_CODE);
        }
    }

    /**
     * @return client/server port number
     */
    final int getPortNumber() {
        return portNumber;
    }

    /**
     * @return host name for client case
     */
    final String getHostName() {
        return hostName;
    }
}
