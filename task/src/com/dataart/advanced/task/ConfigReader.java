package com.dataart.advanced.task;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

import static com.dataart.advanced.task.Info.CONFIG_TAG;
import static com.dataart.advanced.task.Info.SERVER_TAG;
import static com.dataart.advanced.task.Info.CLIENT_TAG;
import static com.dataart.advanced.task.Info.PORT_TAG;
import static com.dataart.advanced.task.Info.HOST_TAG;

/**
 * Configuration file reader based on SAX.
 * Parses specified *.xml - file and stores parameters for client and server in appropriate variables
 *
 * @author Sergey Sokhnyshev
 * Created on 10.04.17.
 */
public class ConfigReader {
    private int portNumber = 0;       // client/server port number
    private String hostName;          // host name for client's usage

    // SAX event handlers processor according to known XML configuration file structure
    class XMLParser extends DefaultHandler {
        private boolean isConfigTag = false;     // flag notifying if CONFIG_TAG is found
        private boolean isClientTag = false;     // flag notifying if CLIENT_TAG is found
        private boolean isServerHost = false;    // flag notifying if HOST_TAG is found

        private boolean isServer = true;  // flag defining client or server parsing purpose

        XMLParser(boolean isServer) {
            this.isServer = isServer;
        }

        // receives notification of the start of an element in XML - configuration file
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (isConfigTag) {
                if (isServer) {
                    if (qName.equalsIgnoreCase(SERVER_TAG)) {
                        portNumber = Integer.parseInt(attributes.getValue(PORT_TAG));
                    }
                }
                else {
                    if (isClientTag) {
                        if (qName.equalsIgnoreCase(HOST_TAG)) {
                            isServerHost = true;
                        }
                    }
                    else if (qName.equalsIgnoreCase(CLIENT_TAG)) {
                        portNumber = Integer.parseInt(attributes.getValue(PORT_TAG));
                        isClientTag = true;
                    }
                }
            }
            else if (qName.equalsIgnoreCase(CONFIG_TAG)) {
                isConfigTag = true;
            }
        }

        // receives notification of the end of an element in XML - configuration file
        @Override
        public void endElement(String uri, String localName,String qName) throws SAXException {
            if (qName.equalsIgnoreCase("config")) {
                isConfigTag = false;
            }
        }

        // receives notification of character data inside an element in XML - configuration file
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isServerHost) {
                hostName = new String(ch, start, length);

                isServerHost = false;
                isClientTag = false;
            }
        }
    }
    /**
     * Reads client/server pagameters from specified *.xml configuration file
     *
     * @param cfgFileName - path and name of *.xml configuration file
     * @param isServer - flag defining the appropriate section in configuration file corresponding to client or server
     *
     * @throws SAXException, IOException
     */
    public void parse(String cfgFileName, boolean isServer) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        XMLParser xmlParser = new XMLParser(isServer);

        File xmlFile = new File(cfgFileName);

        saxParser.parse(xmlFile, xmlParser);
    }

    /**
     * @return client/server port number
     */
    public final int getPortNumber() {
        return portNumber;
    }

    /**
     * @return host name for client case
     */
    public final String getHostName() {
        return hostName;
    }
}
