import java.util.*;

public class mailbox{

    //queue to ensure FIFO sequence of commands
    private static Queue<String> mailbox;

    //constructor
    public mailbox(){
        this.mailbox = new LinkedList<String>();
    }

    //checks if queue is empty
    public static Boolean isEmpty(){
        return mailbox.isEmpty();
    }

    //add command to queue
    public static void add(String command){
        mailbox.add(command);
    }

    //removes command from queue
    public static String remove(){
        String command = mailbox.remove();
        return command;
    }
}
