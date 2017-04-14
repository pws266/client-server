package com.dataart.advanced.task;

/**
 * Simple server messages processing implementation
 *
 * @author Sergey Sokhnyshev
 * Created by newbie on 14.11.16.
 */
public class SimpleClientListener implements ClientListener {
    /**
     * Generates client's responce on received server message
     * @param msg - received server message
     * @return client's response message
     */
    @Override
    public String onProcess(String msg) {
        return "Server: " + msg;
    }
}
