package advanced.task;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Interface for processing received messages
 * It's method is used in "Client" and "Server" classes as callback function
 *
 * @author Sergey Sokhnyshev
 * Created on 10.06.16.
 */
interface Responder<T> {
    void onSetup(T obj);
    String onProcess(String msg);
}

/**
 * Server messages primitive processing by client
 */
class ClientResponder<Client> implements Responder<Client> {
    @Override
    public void onSetup(Client usr) {}

    @Override
    public String onProcess(String msg) {
        return "Server: " + msg;
    }
}

/**
 * Client messages processor
 */
class ServerResponder<Connection> implements Responder<Connection> {
    private Connection connect;  // reference to connection

    @Override
    public void onSetup(Connection connect) {
        this.connect = connect;
    }

    @Override
    public String onProcess(String msg) {
        int msgIndex = Info.notFound;
        int cmdIndex = 0;

        for(String cmd : Info.usrCmd) {
            if ((msg != null) && msg.toLowerCase().contains(cmd)) {
                msgIndex = cmdIndex;
                break;
            }

            ++cmdIndex;
        }

        // return default message for unknown command case
        if (msgIndex == Info.notFound) {
            return Info.srvMsg.get(10);
        }

        // copying predefined server response message
        String srvMsg = Info.srvMsg.get(msgIndex);

        // forming server response message on known user command
        switch (msgIndex) {
            // greeting response
            case 0: srvMsg += ((Server.Connection)connect).getUsrName() + "!";
                    break;
            // client's name response
            case 1: srvMsg += ((Server.Connection)connect).getUsrName() + "\"";
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
            case 6: srvMsg += Integer.toString(((Server.Connection)connect).
                                                 getConnectionsNumber());
                    break;
            // response on user connection index request
            case 7: int connectIndex = ((Server.Connection)connect).
                                         getConnectionIndex();
                    srvMsg += (connectIndex == Info.notFound) ?
                              "not found! Oops!" : Integer.toString(connectIndex);
                    break;
            // response on user commands set request
            case 8: StringBuffer cmdSet = new StringBuffer(srvMsg);

                    for (String s : Info.usrCmd) {
                        cmdSet.append("\n- ").append(s);
                    }

                    srvMsg = cmdSet.toString();
                    break;

        }

        return srvMsg;
    }
}
