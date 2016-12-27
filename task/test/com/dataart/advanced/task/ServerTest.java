package com.dataart.advanced.task;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static com.dataart.advanced.task.Info.*;
import static org.junit.Assert.assertTrue;

/**
 * Server loading test. It performs multiple users connection to server and correct reception verification for each
 * command sent by each user to server.
 *
 * @author Sergey Sokhnyshev
 * Created on 26.11.16.
 */
public class ServerTest {
    private static final int userNumber = 100;
    private static final int commandsNumber = 1000;

    private static final String cfgFileName = "../files/config.xml";

    private Server server;
    private ConfigReader cfgReader = new ConfigReader();

    private List<PayloadClient> client;

    private CountDownLatch latch;

    class PayloadClient implements Runnable {
        private ByteArrayOutputStream inCommand;    // client's commands set
        private ByteArrayOutputStream outCommand;   // commands set processed by server

        private Client client;

        private void generateCommands() throws IOException {
            Random rnd = new Random();

            DataOutputStream out = new DataOutputStream(inCommand);

            for (int i = 0; i < commandsNumber; ++i) {
                String currentCommand = (((i > 0) && (i < commandsNumber - 1)) ?
                                            KNOWN_CMD.get(rnd.nextInt(KNOWN_CMD.size() - 1)).getToken() :
                                            ((i == 0) ? TESTING_USER_NAME.get(rnd.nextInt(TESTING_USER_NAME.size())) :
                                                        QUIT_CMD)) + '\n';

                out.write(currentCommand.getBytes());
            }
        }

        PayloadClient(String hostName, int portNumber) {
            inCommand = new ByteArrayOutputStream();
            outCommand = new ByteArrayOutputStream();

            client = new Client(hostName, portNumber);
            client.setOutputStream(outCommand);

            new Thread(this, TESTING_CLIENT_THREAD_NAME).start();
        }

        @Override
        public void run() {
            try {
                generateCommands();

                ByteArrayInputStream inCommandPool = new ByteArrayInputStream(inCommand.toByteArray());
                client.setInputStream(inCommandPool);

                client.start((String msg) -> msg);

                latch.countDown();
            } catch (IOException exc) {

            }
        }

        final boolean isError() {
            return !Arrays.equals(inCommand.toByteArray(), outCommand.toByteArray());
        }
    }

    @Before
    public void before() throws ParserConfigurationException, SAXException, IOException {
        client = new ArrayList<>(userNumber);
        latch = new CountDownLatch(userNumber);

        cfgReader.parse(cfgFileName, true);
        server = new Server(cfgReader.getPortNumber(), (String msg, Server.Connection connection) -> msg);

        cfgReader.parse(cfgFileName, false);
    }

    @Test
    public void testUserCommandsReception() {
        new Thread(server, SERVER_THREAD_NAME).start();

        IntStream.range(0, userNumber).forEach(i -> client.add(new PayloadClient(cfgReader.getHostName(), cfgReader.getPortNumber())));

        try {
            latch.await();

            long errorsNumber = client.stream()
                                      .filter(PayloadClient::isError)
                                      .count();

            System.out.println("Errors number: " + errorsNumber);
            server.stop();

            assertTrue(errorsNumber == 0);
        } catch(InterruptedException exc) {

        }

    }
}
