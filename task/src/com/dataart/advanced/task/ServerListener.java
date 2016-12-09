package com.dataart.advanced.task;

import static com.dataart.advanced.task.Info.*;

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
