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

    public static final int CMD_NOT_FOUND = -1;
    // default server reply for unknown command token
    public static final String DEFAULT_MSG = "Unknown command. Should I " +
            "consider it like a message to a world?";
    // command for client disconnection
    public static final String QUIT_CMD = "quit";

    // tokens of known user commands
    public static final List<String> USER_CMD = Collections.synchronizedList(
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
        add("id");          // 9 - get client's ID assigned by server
    }});

    // server messages for response forming on server side
    public static final List<String> SERVER_MSG = Collections.synchronizedList(
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
        // 5 - response on rudeness
        add(":) ... Spielberg. Watch your tongue!");
        // 6 - (cmd)response on total connections number request
        add("Total connections number is: ");
        // 7 - (cmd)response on user connection index request
        add("Your connection number is: ");
        // 8 - (cmd)response on user commands set request
        add("The commands could contain these known tokens: ");
        // 9 - (cmd)client's ID response
        add("Client's ID is: ");
    }});
}
