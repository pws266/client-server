package com.dataart.advanced.task;

import java.io.*;

/**
 * Wrapper for I/O usage of console if it is provided by device (e.g. terminal)
 *
 * @author Sergey Sokhnyshev
 * Created on 14.04.17.
 */
public class ConsoleIO implements Closeable {
    private BufferedReader reader;  // reader of text input from standard input if console is inaccessible
    private PrintWriter writer;     // writer for text output to device console or standard output

    private Console console;        // console instance provided by device (e.g. terminal)

    ConsoleIO() {
        // getting console instance: console is inaccessible under IDE
        console = System.console();

        if (console == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
            writer = new PrintWriter(System.out, true);
        }
        else {
            writer = console.writer();
            writer.println("\n> Using I/O via console: " + console + " <\n");
        }
    }

    /**
     * @return string read from device console if it is provided or standard input
     * @throws IOException if reading error occurs
     */
    String readLine() throws IOException{
        return ((console == null) ? reader.readLine() : console.readLine());
    }

    /**
     * @return writer instance for messages output to device console if it is provided or standard output
     */
    PrintWriter out() {
        return writer;
    }

    /**
     * correctly closes reader and writer
     * @throws IOException if error occurs while closing reader and writer
     *
     *  @see Closeable#close
     */
    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }

        writer.close();
    }
}
