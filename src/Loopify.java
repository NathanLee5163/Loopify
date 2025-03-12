import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

public class Loopify implements LoopifyInterface {
    public void run() {
        int logreg = JOptionPane.showConfirmDialog(null,
                "Do you have an account?", "Login or Register", JOptionPane.YES_NO_OPTION);

        if (logreg == JOptionPane.YES_OPTION) {
            String username = JOptionPane.showInputDialog(null, "Enter your username:");
            String password = JOptionPane.showInputDialog(null, "Enter your password:");
            boolean found = false;

            try (BufferedReader br = new BufferedReader(new FileReader("Users.txt"))) {
                String line = br.readLine();


                while (line != null) {
                    String[] data = line.split(",");
                    if (data[0].equals(username) && data[1].equals(password)) {
                        found = true;
                    }
                }
            } catch (Exception e) {


            }

            if (found) {

            }
        } else {
            //Send processing to LoopifyServer class. Make LoopifyServer
            while (true) {
                String username = JOptionPane.showInputDialog(null, "Enter your username:");
                String password = JOptionPane.showInputDialog(null, "Enter your password:");
                //Check that password (one capital letter, one symbol, and 1 number, and at least 8 characters)
                //Check that username has a space

                try {
                    User newUser = new User(username, password);
                } catch (Exception e) {

                }

            }
        }




    }

    public static void main(String[] args) {
        Loopify thread = new Loopify();
        thread.run();
    }
}
