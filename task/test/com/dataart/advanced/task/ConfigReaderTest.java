package com.dataart.advanced.task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

/**
 * Values verification read by ConfigReader from specified *.xml configuration file in different modes
 *
 * @author Sergey Sokhnyshev
 * Created on 06.09.16.
 */
public class ConfigReaderTest {
    private static final String cfgFileName = "../files/config.xml";

    private static final int correctPortNumber = 8000;
    private static final String correctHostName = "localhost";

    private ConfigReader cfgReader;

    @Before
    public void before() {
        cfgReader = new ConfigReader();
    }

    @Test
    public void testServerCfgLoading() throws ParserConfigurationException, SAXException, IOException {
        cfgReader.parse(cfgFileName, true);

        Assert.assertEquals(cfgReader.getPortNumber(), correctPortNumber);
    }

    @Test
    public void testClientCfgLoading() throws ParserConfigurationException, SAXException, IOException {
        cfgReader.parse(cfgFileName, false);

        Assert.assertEquals(cfgReader.getPortNumber(), correctPortNumber);
        Assert.assertEquals(cfgReader.getHostName(), correctHostName);
    }
}