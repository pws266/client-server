package advanced.task;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by newbie on 15.06.16.
 */
public class DummyTest {
    public static void main(String[] args) {
        BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter your name: ");
        String userName = "";

        try {
           userName  = cmdIn.readLine();
        } catch(IOException exc) {
            exc.printStackTrace();
        }

        System.out.println("You have entered \"" + userName + "\"");

        String cmdFlow = "Wagner\nis\na\nstupid\npig\n";

        cmdIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdFlow.getBytes())));
        String line = "";
        try {
            line = cmdIn.readLine();
        } catch(IOException exc) {
            exc.printStackTrace();
        }

        System.out.println("You line is \"" + line + "\"");


    }
}
