package com.dataart.advanced.task;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server loading test. It performs multiple users connection to server and correct reception verification for each
 * command sent by each user to server.
 *
 * @author Sergey Sokhnyshev
 * Created on 26.11.16.
 */
public class ServerTest {
    private static final String cfgFileName = "../files/config.xml";

    private static final int correctPortNumber = 8000;
    private static final String correctHostName = "localhost";

    @Before
    public void before() throws ParserConfigurationException, SAXException, IOException {
        ConfigReader cfgReader = new ConfigReader();
        cfgReader.parse(cfgFileName, true);

        Server server = new Server(cfgReader.getPortNumber(), (String msg, Server.Connection connection) -> msg);

        cfgReader.parse(cfgFileName, false);
    }

    @Test
    public void testUserCommandsReception() {
/*
        ExecutorService executor = Executors.newSi;

        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            for (int i = 0; i < 10; ++i) {
                System.out.println("Thread #" + threadName + " Counter #" + i);
            }
        });
*/
    }
}
