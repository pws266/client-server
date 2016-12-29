package com.dataart.advanced.task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static com.dataart.advanced.task.Info.CONFIG_TAG;
import static com.dataart.advanced.task.Info.SERVER_TAG;
import static com.dataart.advanced.task.Info.CLIENT_TAG;
import static com.dataart.advanced.task.Info.PORT_TAG;
import static com.dataart.advanced.task.Info.HOST_TAG;

/**
 * Configuration file reader.
 * Parses specified *.xml - file and stores parameters for client and server
 * in appropriate variables
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
class ConfigReader {
    private int portNumber = 0; // client/server port number
    private String hostName;    // host name for client's usage

    /**
     * Reads parameters list from specified *.xml configuration file
     *
     * @param cfgFileName - path and name of *.xml configuration file
     * @param isServer - flag defining the appropriate section in configuration file corresponding to client or server
     *
     * @return parameters nodes list for further parsing
     *
     * @throws ParserConfigurationException, SAXException, IOException
     */
    private NodeList readParametersList(String cfgFileName, boolean isServer) throws ParserConfigurationException,
                                                                                     SAXException, IOException {
        File cfgFile = new File(cfgFileName);

        // reading whole *.xml document structure
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(cfgFile);

        doc.getDocumentElement().normalize();

        // starting to parse XML body
        NodeList cfgList = doc.getElementsByTagName(CONFIG_TAG);

        Node cfgNode = cfgList.item(0);

        if (cfgNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new IOException("Element node isn't found in \"" + cfgFileName + "\" while reading parameters list." +
                                  "Illegal *.xml file format");
        }

        Element cfgElm = (Element)cfgNode;

        String dstTag = isServer ? SERVER_TAG : CLIENT_TAG;
        return cfgElm.getElementsByTagName(dstTag);
    }

    /**
     * Reads port number value from specified parameters nodes list
     *
     * @param paramList - parameters nodes list
     *
     * @return element node for further parsing
     * @throws IOException - if required node in XML is not found
     */
    private Element readPortNumber(NodeList paramList) throws IOException {
        Node dstNode = paramList.item(0);

        if (dstNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new IOException("Element node isn't found while reading port number. Illegal *.xml file format");
        }

        Element dstElm = (Element)dstNode;
        portNumber = Integer.parseInt(dstElm.getAttribute(PORT_TAG));

        return dstElm;
    }

    /**
     * Reads host name from specified element of *.xml configuration file
     *
     * @param elm - element of *.xml configuration file containing host name
     * @param isServer - flag defining the appropriate section in configuration file corresponding to client or server
     *
     * @throws IOException - if required element in XML is not found
     */
    private void readHostName(Element elm, boolean isServer) throws IOException {
        if (isServer) {
            hostName = null;
            return;
        }

        NodeList hostList = elm.getElementsByTagName(HOST_TAG);

        Node hostNode = hostList.item(0);
        if (hostNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new IOException("Element node isn't found while reading host name. Illegal *.xml file format");
        }

        NodeList list = hostNode.getChildNodes();
        hostName = list.item(0).getNodeValue();
    }

    /**
     * Reads client/server pagameters from specified *.xml configuration file
     *
     * @param cfgFileName - path and name of *.xml configuration file
     * @param isServer - flag defining the appropriate section in configuration file corresponding to client or server
     *
     * @throws ParserConfigurationException, SAXException, IOException
     */
    void parse(String cfgFileName, boolean isServer) throws ParserConfigurationException, SAXException, IOException{
        NodeList paramList = readParametersList(cfgFileName, isServer);

        Element hostElm = readPortNumber(paramList);
        readHostName(hostElm, isServer);
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
