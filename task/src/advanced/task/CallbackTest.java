package advanced.task;

/**
 * Created by newbie on 30.06.16.
 */
interface Listener {
    String onProcess(String msg);
}

public class CallbackTest {
    Listener listener;
    String msg = "Wagner > ";

    public String getMsg() {
        return msg;
    }

    String getResponse(Listener listener) {
        return listener.onProcess("blah-blah");
    }

    public static void main(String[] args) {
        CallbackTest test = new CallbackTest();
        String res = test.getResponse(new Listener() {
            CallbackTest cb = test;
            @Override
            public String onProcess(String msg) {
                return test.getMsg() + msg;
            }
        });

        System.out.println(res);
    }
}
