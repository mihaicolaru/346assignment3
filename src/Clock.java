import java.io.*;

//clock function was adapted from tutorial
//all function of the class is the same
//thread sleep and increment times are stored as final values at the top of the method for testing purposes
public enum Clock implements Runnable{
    INSTANCE(1000); 
    private int time;
    public static final int THREAD_SLEEP_TIME = 100; 
    public static final int TIME_INCREMENT = 10;
    private boolean isComplete;
    File output;

    Clock(int i){
        time = i;
        isComplete = false;
        output = new File("output.txt");
        printEvent("", false);

    }

    //same functions as seen in tutorial
    public int getTime(){
        return time;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    //altered to print out to output.txt instead
    public void printEvent(String ev){
        OutputToFile.write(output, ev, true);
    }

    //overloaded print which also takes boolean value to give option on whether to append or overwrite
    public void printEvent(String ev, Boolean append){
        OutputToFile.write(output, ev, append);
    }


    @Override
    public void run(){
        while(!isComplete){
            try{
                Thread.sleep(THREAD_SLEEP_TIME);
            }catch(Exception e){
                e.printStackTrace();
            }
            time = time + TIME_INCREMENT;
        }
    }
}
