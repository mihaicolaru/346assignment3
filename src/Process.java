import java.io.*;
import java.util.*;

public class Process implements Runnable{
    private int serviceTime;
    private int readyTime;
    private String ID;
    private int processState;
    //0: waiting
    //1: ready
    //2: running
    //3: terminated

    //shared variables between processes
    static int totalLines;
    static int line;

    //getters and setters
    public void setTotalLines(int totalLines){
        Process.totalLines = totalLines;
    }

    public int getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(int readyTime) {
        this.readyTime = readyTime;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public int getProcessState() {
        return processState;
    }

    public void setProcessState(int state) {
        this.processState = state;
    }

    //synchronized to protect reading commands, updating shared line value and sending to mailbox
    public synchronized void InputReader() {
        String command = new String();

        try{
            File file = new File("commands.txt");
            Scanner scan = new Scanner(file);

            for(int i = 0; i < line; i++){
                scan.nextLine();
            }

            command = scan.nextLine();

            //starts line back from beginning when it has reached the end
            line = (line + 1) % totalLines;

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        //add process ID to command string and send to mailbox
        String mail = ID + " " + command;
        mailbox.add(mail);
    }

    //assign random amount of command exec time: (1ms <= time <= 1000ms) < service time
    public int generateTime(){
        int timeLeft = (readyTime + serviceTime)*1000 - Clock.INSTANCE.getTime();
        int generatedTime = 0;
        if(timeLeft > 0){
            Random rand = new Random();
            int upperBound = timeLeft;

            //if the time left is more than 1000, set upperBound to 1000
            if(upperBound > 1000){
                upperBound = 1000;
            }
            generatedTime = rand.nextInt(upperBound) + 1;
        }
        return generatedTime;
    }


    @Override
    public void run(){
        //print process started message
        String started = "Clock: " + Clock.INSTANCE.getTime() + ", Process " + ID + ": Started.";
        Clock.INSTANCE.printEvent(started);

        //set finishTime
        int finishTime = Clock.INSTANCE.getTime();
        finishTime = finishTime + serviceTime*1000;
        int commandTime = 0;//0 commandTime means it will get assigned on the first loop
        do {
            //check if process finished execution
            if(finishTime <= Clock.INSTANCE.getTime()){
                processState = 3;
                String finished = "Clock: " + Clock.INSTANCE.getTime() + ", Process " + ID + ": Finished";
                Clock.INSTANCE.printEvent(finished);
            }

            //if process running
            if(processState == 2){
                //if process has elapsed commandTime
                if(commandTime < Clock.INSTANCE.getTime()){
                    //assign random commandTime
                    commandTime = generateTime();
                    //call inputReader to send command to mailbox
                    InputReader();
                    //add current time to commandTime so process executes for time = commandTime
                    commandTime += Clock.INSTANCE.getTime();
                }
            }
            try{
                Thread.sleep(10);
            }catch(Exception e){
                e.printStackTrace();
            }
        } while(processState != 3);
    }
}
