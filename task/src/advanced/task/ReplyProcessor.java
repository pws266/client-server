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
 *
 */
class ReplyOnServerMsg implements ReplyProcessor {
    @Override
    public String onProcess(String msg) {
        return null;
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

