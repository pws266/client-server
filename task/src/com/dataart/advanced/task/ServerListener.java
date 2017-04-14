package com.dataart.advanced.task;

/**
 * Interface for clients messages processing received by server
 *
 * @author Sergey Sokhnyshev
 * Created on 06.07.16.
 */
public interface ServerListener {
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
