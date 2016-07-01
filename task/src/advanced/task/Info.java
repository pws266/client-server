package advanced.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reference information for client-server application.
 * The class stores numeric constants, strings, etc. using in project classes
 *
 * @author Sergey Sokhnyshev
 * Created on 14.06.16.
 */
public class Info {
    /**
     * Private constructor for preventing class instances creation
     */
    private Info() {
        throw new AssertionError();
    }

    // exit codes

    public static final int notFound = -1;

    // tokens of known user commands
    public static final List<String> usrCmd = Collections.synchronizedList(
                                              new ArrayList<String>() {{
        add("hello");       // 0 - greeting
        add("my name");     // 1 - ask client's name
        add("your name");   // 2 - ask server's name
        add("time");        // 3 - ask current time
        add("date");        // 4 - ask current date
        add("fuck");        // 5 - swear an oath
        add("total");       // 6 - ask total connections number
        add("my number");   // 7 - ask connection index
        add("help");        // 8 - get maintained user commands set
        add("quit");        // 9 - disconnect and exit
    }});

    // server messages for response forming on server side
    public static final List<String> srvMsg = Collections.synchronizedList(
                                              new ArrayList<String>() {{
        // 0 - (cmd)greeting response
        add("Hi, ");
        // 1 - (cmd)client's name response
        add("Hmm... you introduced as \"");
        // 2 - server's name response
        add("\"You can call me Susan if it makes you happy\"(c)Snatch");
        // 3 - (cmd)response to current time request
        add("The current time is: ");
        // 4 - (cmd)response to current date request
        add("Today is: ");
        // 5 - responce on rudeness
        add(":) ... Spielberg. Watch your tongue!");
        // 6 - (cmd)response on total connections number request
        add("Total connections number is: ");
        // 7 - (cmd)response on user connection index request
        add("Your connection number is: ");
        // 8 - (cmd)response on user commands set request
        add("I know next commands: ");
        // 9 - (cmd)disconnect message
        add("You are disconnected. Have a nice day, ");
        //10 - default message
        add("Unknown command. Should I consider it like a message to a world?");
    }});
}
