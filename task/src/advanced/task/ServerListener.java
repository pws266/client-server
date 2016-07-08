package advanced.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import static advanced.task.Info.*;

/**
 * Interface for clients messages processing received by server
 *
 * @author Sergey Sokhnyshev
 * Created on 06.07.16.
 */
interface ServerListener {
    /**
     * Generates server's response on received client message
     *
     * @param msg - received client message
     * @param connection - reference to connection with client for AI response
     *                     forming
     * @return server response message
     */
    String onProcess(String msg, Server.Connection connection);
}

/**
 * Clients messages processing implementation based on trivial AI
 */
class AIServerListener implements ServerListener {
    /**
     * Generates server's response on received client message. Searches known
     * tokens in client message and creates answer based on its
     *
     * @param msg - received client message
     * @param connection - reference to connection with client for AI response
     *                     forming
     * @return server response message
     */
    @Override
    public String onProcess(String msg, Server.Connection connection) {
        int msgIndex = CMD_NOT_FOUND;
        int cmdIndex = 0;

        for(String cmd : USER_CMD) {
            if ((msg != null) && msg.toLowerCase().contains(cmd)) {
                msgIndex = cmdIndex;
                break;
            }

            ++cmdIndex;
        }

        // return default message for unknown command case
        if (msgIndex == CMD_NOT_FOUND) {
            return DEFAULT_MSG;
        }

        // copying predefined server response message
        String srvMsg = SERVER_MSG.get(msgIndex);

        // forming server response message on known user command
        switch (msgIndex) {
            // greeting response
            case 0: srvMsg += connection.getUsrName() + "!";
                    break;
            // client's name response
            case 1: srvMsg += connection.getUsrName() + "\"";
                    break;
            // response to current time request
            case 3: srvMsg += (new SimpleDateFormat("kk:mm:ss XXX")).
                    format(new Date());
                    break;
            // response to current date request
            case 4: srvMsg += (new SimpleDateFormat("EEE, MMM dd, yyyy")).
                    format(new Date());
                    break;
            // response on total connections number request
            case 6: srvMsg += Integer.toString(connection.
                    getConnectionsNumber());
                    break;
            // response on user connection index request
            case 7: int connectIndex = connection.getConnectionIndex();
                    srvMsg += (connectIndex == CMD_NOT_FOUND) ?
                        "not found! Oops!" : Integer.toString(connectIndex);
                    break;
            // response on user commands set request
            case 8: StringBuffer cmdSet = new StringBuffer(srvMsg);

                    for (String s : USER_CMD) {
                        cmdSet.append("\n- ").append(s);
                    }

                    srvMsg = cmdSet.toString();
                    break;

            // response on client's ID request
            case 9: srvMsg += Integer.toString(connection.getClientID());
                    break;
        }

        return srvMsg;
    }
}


