package advanced.task;

/**
 * Interface for processing received messages
 * It's method is used in "Client" and "Server" classes as callback function
 *
 * @author Sergey Sokhnyshev
 * Created on 10.06.16.
 */
interface ReplyProcessor {
    String onProcess(String msg);
}

/**
 * Server messages primitive processing by client
 */
class ReplyOnServerMsg implements ReplyProcessor {
    @Override
    public String onProcess(String msg) {
        return "Server: " + msg;
    }
}

/**
 *
 */
class ReplyOnClientMsg implements ReplyProcessor {
    @Override
    public String onProcess(String msg) {
        return null;
    }
}

