import java.io.File;
import java.io.*;
import java.util.*;
import java.io.FileNotFoundException;

public class Scheduler implements Runnable{

    private int numCores;//number of cores

    private final List<Process> Processes;//processes that have not finished execution
    private final List<Process> readyProcesses;//processes that are ready
    private final List<Thread> processThreads;//threads of running processes

    private final List<Process> cores;//list of running processes

    //constructor initializes variables and calls inputReader
    public Scheduler(){

        this.numCores = 0;

        this.Processes = new ArrayList<Process>();
        this.readyProcesses = new ArrayList<Process>();
        this.cores = new ArrayList<Process>();
        this.processThreads = new ArrayList<Thread>();

        InputReader();
    }

    //returns number of cores
    public int GetAvailableCores()
    {
        return numCores - cores.size();
    }

    //reads processes.txt and instantiates processes and number of lines in commands.txt
    public void InputReader(){
        try{
            File file = new File("processes.txt");
            Scanner scan = new Scanner(file);

            numCores = scan.nextInt();
            scan.nextInt();

            Integer processID = 1;
            while(scan.hasNextLine()){
                Process newProcess = new Process();

                newProcess.setReadyTime(scan.nextInt());
                newProcess.setServiceTime(scan.nextInt());

                newProcess.setID(processID.toString());
                Processes.add(newProcess);

                processID++;
            }
            scan.close();

            File commands = new File("commands.txt");
            Scanner scan2 = new Scanner(commands);
            int totalLines = 0;
            while (scan2.hasNextLine()) {
                scan2.nextLine();
                totalLines++;
            }
            Processes.get(0).setTotalLines(totalLines);
            scan2.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    //check list of processes for ready processes
    public void updateReadyQueue(){
        if(Processes.size() > 0) {
            for (Process process : Processes) {

                if (process.getReadyTime() <= Clock.INSTANCE.getTime()/1000 && process.getProcessState() == 0) {
                    readyProcesses.add(process);
                    process.setProcessState(1);
                }
            }
        }
    }

    //assign ready processes to available core
    public void assignToCore(){
        while(GetAvailableCores() > 0 && readyProcesses.size() > 0){
            cores.add(readyProcesses.remove(0));//assigns first ready process to core, shifts other processes to the left, starts process
        }
    }

    //check cores for processes, create and start process thread
    public void executeProcess(){
        if(cores.size() > 0){
            for(Process process : cores){
                if(process.getProcessState() == 1){
                    process.setProcessState(2);//set to running

                    Thread processThread = new Thread(process);
                    processThread.setName(process.getID());
                    processThreads.add(processThread);
                    processThread.start();
                }
            }
        }
    }

    //check process state, if done, remove from process list and join thread
    public void terminateProcess(){
        if(cores.size() > 0){
            for(Process process : cores){

                if(process.getProcessState() == 3){

                    for (Thread processThread : processThreads) {
                        if (processThread.getName().equals(process.getID()) ) {
                            try {
                                processThread.join();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    cores.remove(process);
                    Processes.remove(process);
                }
                if(cores.size() <= 0) break;//this fixes the crash
            }
        }
    }

    @Override
    public void run(){
        //process scheduling loop
        while(!Processes.isEmpty()) {
            //pointers to time to ensure scheduling loop is 1sec
            int start = Clock.INSTANCE.getTime();
            int ctime = Clock.INSTANCE.getTime();

            terminateProcess();//was causing lag problems with synchronization works better when called at the beginning of the cycle

            updateReadyQueue();

            assignToCore();

            executeProcess();

            //1000ms cycles but only 990 to allow for processes to catch up
            while(ctime-start <= 990){
                try{
                    Thread.sleep(10);
                }catch(Exception e){
                    e.printStackTrace();
                }
                ctime = Clock.INSTANCE.getTime();
            }
        }
    }
}
