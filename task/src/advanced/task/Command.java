package advanced.task;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Commands exchange protocol implementation.
 * The command format is:
 * - 4 bytes -> length of whole command in bytes (excluding command length
 *              itself). Should be read first via "readInt()";
 * - 4 bytes -> client's ID assigned by server after connection;
 * - variable number of bytes -> command body.
 * Supports multi-line commands.
 *
 * @author Sergey Sokhnyshev
 * Created on 01.07.16.
 */

/**
 * Client/server command representation.
 * Field "isEOF" is notifying whether the stream reading should be stopped
 * or not
 */
class CommandTraits {
    static final int DEFAULT_ID = -1;  // client's ID default value

    int clientID = DEFAULT_ID;  // client ID number
    String msg = "";            // message or command content

    boolean isEOF = false;      // flag notifying about end of stream

    /**
     * Constructor for command creation
     * @param msg - command/message content
     * @param clientID - client's ID who send/receive this command
     */
    CommandTraits(String msg, int clientID) {
        this.msg = msg;
        this.clientID = clientID;
    }
}

/**
 * Commands processing
 */
public class Command {
    // bytes number for message size and client's ID
    private static final int FIELD_SIZE = 4;
    private static final int HEADER_SIZE = 8;
    // service symbols substitutions
    private final static Map<String, String> SYMBOL_SUBSTITUTION =
                        new HashMap<String, String>() {{
        put("\\n", "\n");
        put("\\r", "\r");
        put("\\0", "\0");
    }};

    private static final int DEFAULT_SZ = -1;  // default command size value
    private static final int COMMAND_EXIT_CODE = 6;  // exit code
    // logger for tracing error messages
    private static final Logger log = Logger.getLogger(Command.class.getName());

    /**
     * Transforms command fields to byte sequence according aforementioned
     * format
     *
     * @param cmd - command for packing
     * @return array of bytes representing packed command. The array length is
     *         stored in first 4 bytes of obtained sequence
     */
    static byte[] pack(CommandTraits cmd) {
        // searching for service symbols
        StringBuffer msgBuffer = new StringBuffer(cmd.msg);

        for(Map.Entry<String, String> entry : SYMBOL_SUBSTITUTION.entrySet()) {
            int findIndex = 0;

            while ((findIndex = msgBuffer.indexOf(entry.getKey(),
                    findIndex)) != -1) {
                msgBuffer.replace(findIndex,
                        findIndex + entry.getKey().length(),
                        entry.getValue());
            }
        }

        // creating command according transport protocol
        byte[] body = new byte[HEADER_SIZE + msgBuffer.length()];

        // copying command size (client's ID size + message size)
        ByteBuffer bb = ByteBuffer.wrap(body, 0, FIELD_SIZE);
        bb.putInt(FIELD_SIZE + msgBuffer.length());

        // copying client's ID value
        bb = ByteBuffer.wrap(body, FIELD_SIZE, FIELD_SIZE);
        bb.putInt(cmd.clientID);

        // copying command content
        System.arraycopy(msgBuffer.toString().getBytes(), 0, body, HEADER_SIZE,
                         msgBuffer.length());

        return body;
    }

    /**
     * Extracts command representation from specified byte sequence. The full
     * byte sequence size should be obtained first via reading of first 4 bytes
     * @param cmdBody - byte sequence representing command without first 4 bytes
     *                  containing whole sequence length (could be read in
     *                  stream via "readInt()")
     * @param cmdSize - residual byte sequence length (excluding first 4 bytes)
     * @return command representation as set of fields
     */
    private static CommandTraits unpack(byte[] cmdBody, int cmdSize) {
        // getting client's ID
        ByteBuffer bb = ByteBuffer.wrap(cmdBody, 0, FIELD_SIZE);

        return new CommandTraits(new String(cmdBody, FIELD_SIZE,
                                 cmdSize - FIELD_SIZE), bb.getInt());
    }

    /**
     * Reads command from data stream
     * @param is - input stream transferring commands between client and server
     * @return command representation as set of fields
     */
    static CommandTraits receive(DataInputStream is) {
        // getting whole command size
        int cmdSz = DEFAULT_SZ;

        try {
            cmdSz = is.readInt();
        } catch (EOFException exc) {
            CommandTraits cmdEOF = new CommandTraits("",
                                                     CommandTraits.DEFAULT_ID);
            cmdEOF.isEOF = true;

            return cmdEOF;
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Command: error reading command size", exc);
            System.exit(COMMAND_EXIT_CODE);
        }

        // reading command content from input stream
        byte[] cmdBody = new byte[cmdSz];
        int readSz = DEFAULT_SZ;

        try {
            if((readSz = is.read(cmdBody, 0, cmdSz)) != cmdSz) {
                throw new Exception("Command: declared command size doesn't " +
                                    "correspond to read body size");
            }
        } catch (IOException exc) {
            log.log(Level.SEVERE, "Command: unable to read command body", exc);
            System.exit(COMMAND_EXIT_CODE);
        } catch (Exception exc) {
            log.log(Level.SEVERE, "Command: command declared (" + cmdSz +
                                  " bytes) and obtained (" + readSz +
                                  " bytes) sizes mismatch", exc);
            System.exit(COMMAND_EXIT_CODE);
        }

        return Command.unpack(cmdBody, cmdSz);
    }
}
