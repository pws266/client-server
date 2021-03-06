package com.dataart.advanced.task;

/**
 * Interface for server messages processing received by client
 *
 * @author Sergey Sokhnyshev
 * Created on 06.07.16.
 */
public interface ClientListener {
    /**
     * Generates client's response on received server message
     * @param msg - received server message
     * @return client's response message
     */
    String onProcess(String msg);
}
