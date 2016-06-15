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

/**
 * Configuration file reader.
 * Parses specified *.xml - file and stores parameters for client and server
 * in appropriate variables
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
class ConfigReader {
    // client/server port number
    private int portNumber = 0;
    // host name for client's usage
    private String hostName;

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
            NodeList cfgList = doc.getElementsByTagName(Info.configTag);

            Node cfgNode = cfgList.item(0);
            if (cfgNode.getNodeType() == Node.ELEMENT_NODE) {
                Element cfgElm = (Element)cfgNode;

                // reading port value for client/server
                String dstTag = isServer ? Info.serverTag : Info.clientTag;
                NodeList dstList = cfgElm.getElementsByTagName(dstTag);

                Node dstNode = dstList.item(0);
                if (dstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dstElm = (Element)dstNode;
                    portNumber = Integer.parseInt(dstElm.getAttribute(
                                                  Info.portTag));

                    // getting host name for client
                    if (!isServer) {
                        NodeList hostList = dstElm.getElementsByTagName(
                                                   Info.hostTag);
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
            System.out.println("ConfigReader error: unable to get DOM " +
                               "document instance from XML");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        } catch(org.xml.sax.SAXException exc) {
            System.out.println("ConfigReader error: unable to parse given " +
                               "XML content");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        } catch(IOException exc) {
            System.out.println("ConfigReader error: some I/O problems occur " +
                               "while parsing XML");
            System.out.println("Error description: " + exc.getMessage());
            //TODO: add logging
        }
    }

    /**
     * Returns client/server port number
     */
    final int getPortNumber() {
        return portNumber;
    }

    /**
     * Returns host name for client case
     */
    final String getHostName() {
        return hostName;
    }
}
