package com.dataart.advanced.task;

import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.dataart.advanced.task.Info.*;
import static com.dataart.advanced.task.Info.CONNECTION_WELCOME_MSG;

/**
 * Clients messages processing implementation based on trivial AI
 *
 * @author Sergey Sokhnyshev
 * Created on 14.11.16.
 */
class AIServerListener implements ServerListener {
    /**
     * Interface for request processing specified by received known client's
     * token performing on server side
     * @param <T> - returned value type substituting in response
     */
    interface Action<T> {
        /**
         * Client's request processing specified by received known token
         * @param connection - reference on connection associated with given
         *                     client
         * @return result of request processing on server side
         */
        T make(Server.Connection connection);
    }

    /**
     * Request for getting client's name
     */
    class UserNameAction implements Action<String> {
        /**
         * Client's name request processing
         * @param connection - reference on connection associated with given
         *                     client
         * @return client's name corresponding to given connection
         */
        @Override
        public String make(Server.Connection connection) {
            return connection.getUsrName();
        }
    }

    /**
     * Request for getting current time/date from server
     */
    class TimeAction implements Action<String> {
        private final SimpleDateFormat time;  // instance for time/date format
        private final Lock readLock = new ReentrantReadWriteLock().readLock();

        /**
         * Constructor assigning specified time/date format pattern
         * @param pattern - time/date pattern for output
         */
        TimeAction(String pattern) {
            time = new SimpleDateFormat(pattern);
        }

        /**
         * Current time/date request processing
         * @param connection - reference on connection associated with given
         *                     client
         * @return current time/date in string representation according to
         *         specified format
         */
        @Override
        public String make(Server.Connection connection) {
            readLock.lock();

            try{
                return time.format(System.currentTimeMillis());
            } finally {
                readLock.unlock();
            }
        }
    }

    /**
     * Request for getting total connections number to server
     */
    class TotalConnectionsAction implements Action<Integer> {
        /**
         * Total connections number request processing
         * @param connection - reference on connection associated with given
         *                     client
         * @return total connections number
         */
        @Override
        public Integer make(Server.Connection connection) {
            return connection.getConnectionsNumber();
        }
    }

    /**
     * Request for getting given connection index in connections pool
     */
    class ConnectionIndexAction implements Action<String> {
        /**
         * Connection index request processing
         * @param connection - reference on connection associated with given
         *                     client
         * @return connections index in connections pool on server side
         */
        @Override
        public String make(Server.Connection connection) {
            int connectionIndex = connection.getConnectionIndex();
            return connectionIndex == CMD_NOT_FOUND ? "not found" : Integer.toString(connectionIndex);
        }
    }

    /**
     * Request for getting client's ID number
     */
    class ClientIDAction implements Action<Integer> {
        /**
         * Client's ID numberrequest processing
         * @param connection - reference on connection associated with given
         *                     client
         * @return client's ID number specified by given connection
         */
        @Override
        public Integer make(Server.Connection connection) {
            return connection.getClientID();
        }
    }

    /**
     * Client's known command description
     */
    class UserCmd {
        private final String token;     // command token
        private String response;  // response on known token

        private final Action action;    // request processing specified by token

        /**
         * Constructor creates known command description
         * @param token - command token
         * @param response - response preamble
         * @param action - request processing corresponding to token
         */
        UserCmd(String token, String response, Action action) {
            this.token = token;
            this.response = response;

            this.action = action;
        }

        /**
         * Process request to server and forms response on appropriate token
         * @param connection - reference on connection associated with given
         *                     client
         * @return complete response on received token including request
         *         processing result
         */
        String getAnswer(Server.Connection connection) {
            return action == null ? response : String.format(response, action.make(connection));
        }

        /**
         * @return command token
         */
        final String getToken() {
            return token;
        }

        /**
         * @return command response
         */
        final String getResponse() {
            return response;
        }

        /**
         * Assigns command response
         * @param responce command response externally specified
         */
        void setResponse(String responce) {
            this.response = responce;
        }
    }

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
        // searching specified token and reply in collection
        UserCmd opt = KNOWN_CMD.stream()
                .filter(t -> connection.isUserNameReceived() && msg.toLowerCase().contains(t.getToken()))
                .findAny()
                .orElse(connection.isUserNameReceived() ? DEFAULT_CMD :
                        new AIServerListener().new UserCmd(msg, String.format(CONNECTION_GREETENG_MSG +
                                                                              CONNECTION_WELCOME_MSG, msg), null));

        return opt.getAnswer(connection);
    }
}
