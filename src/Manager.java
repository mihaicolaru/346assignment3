
import java.util.*;
import java.io.*;

public class Manager implements Runnable {
    private boolean firstWriteToVM;
    private boolean done;

    private int memUsed; //current amount stored in memory
    private int memSize; //max value that can be stored in memory

    private Scanner diskScanner; //scanner for vm
    private File vm; //file accessed by scanners

    private List<Page> mainMemory; //data structure to hold main memory pages
    private HashMap<String, Integer> LastAccessValue; //page to LastAccess

    public Manager() { //manager constructor
        this.done = false; //set to true when manager is complete
        this.memUsed = 0; //initially no memory is used
        this.memSize = InputReader(); //set value in memconfig to number of pages
        vm = new File("vm.txt"); //declare file variable to path of vm
        OutputToFile.write(vm, "", false); //clear vm.txt
        this.mainMemory = new ArrayList<>(memSize); //define a main memory of given size
        this.LastAccessValue = new HashMap<>(memSize); //define hashmap of given memory size

    }

    public void setDone(Boolean done) {
        this.done = done;
    } //setter to define that manger is complete its process

    public void interpretCommand(String command) { //interpret and call the command found in mailbox
        String[] currentCommand = command.split(" "); //split to identify values of string
        String processID = currentCommand[0]; //first value sent is the process id

        switch (currentCommand[1]) { //switch based on the command called
            case "Store" -> {
                Store(currentCommand[2], Integer.parseInt(currentCommand[3]));
                String store = "Clock: "+Clock.INSTANCE.getTime()+", Process "+processID+", Store: Variable "+currentCommand[2]+", Value: " + currentCommand[3];
                Clock.INSTANCE.printEvent(store); //output to file
            }
            case "Release" -> {
                Release(currentCommand[2]);
                String release = "Clock: "+Clock.INSTANCE.getTime()+", Process "+processID+", Release: Variable "+currentCommand[2];
                Clock.INSTANCE.printEvent(release);
            }
            case "Lookup" -> {
                int LocatedVal = Lookup(currentCommand[2]);
                String lookup = "Clock: "+Clock.INSTANCE.getTime()+", Process "+processID+", Lookup: Variable "+currentCommand[2]+", Value " + LocatedVal;
                Clock.INSTANCE.printEvent(lookup);
            }
        }
    }

    //check if this works on empty lines
    public boolean FindInVM(String Key) { //function to iterate through vm.txt and find a given id
        boolean Found = false; //if key is found value is set to true

        try {//open scanner to read through vm.txt
            diskScanner = new Scanner(vm);
            while (diskScanner.hasNextLine())
            {
                String[] line = diskScanner.nextLine().split(" ");
                if (line[0].equals(Key)) //if the variable being looked at is equivalent to the given key
                {
                    Found = true; //the variable is found
                    diskScanner.close(); //the scanner can be close
                    break; //further iteration is not needed
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Found; //return whether the value was found
    }


    synchronized private void overwriteVM(String find, String replace) { //overwrite the value find with replace in vm
        StringBuffer MyString = new StringBuffer(); //structure used to save vm pages

        diskScanner.close(); //close disk scanner in case its open
        try {
            diskScanner = new Scanner(vm); //declare new scanner on vm.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (diskScanner.hasNextLine()) { //while there is another line
            String currentString = diskScanner.nextLine(); //assign the current line to a string
            String[] line = currentString.split(" ");
            if(line[0].equals(find)){ //if variable id is equal to find
                if(replace != null) { //replace is not null
                    if (diskScanner.hasNextLine()) //and next line is not an empty space
                        MyString.append(replace + System.lineSeparator()); //append and bring cursor to next line
                    else  //just replace with no line separator
                    {
                        MyString.append(replace); //if no value is left to read do not include line separator
                    }
                }
            }else{
                if(diskScanner.hasNextLine()) //and current line is not an empty space
                    MyString.append(currentString + System.lineSeparator()); //from tutorials point link
                else
                {
                    MyString.append(currentString); //if no value is left to read do not include line separator
                }
            }
        }
        diskScanner.close();
        OutputToFile.write(vm, MyString.toString(), false); //write everything save to string buffer to vm.txt
    }

    public void Store(String variable, int value) {
        Page pageToStore = new Page(variable, value); //temporary page to compare to
        boolean savedToMem = false;

        //if page already exists in main memory, update last accessed value
        for (Page currentPage : mainMemory) {
            if (currentPage.getID().equals(pageToStore.getID()))
            {
                LastAccessValue.put(pageToStore.getID(), Clock.INSTANCE.getTime()); //add new mapping
                currentPage.setValue(value);
                savedToMem = true;
            }
        }

        //if the value was not save to memory
        if(!savedToMem){

        if(memUsed < memSize){//if space is available in memory
            if(!FindInVM(variable)) {//if the given value is not already in vm, adds to memory
                memUsed++;
                mainMemory.add(pageToStore);
                LastAccessValue.put(pageToStore.getID(), Clock.INSTANCE.getTime());
            }
            else //if the given value can be found in vm, it is overwritten
            {
                overwriteVM(variable, pageToStore.toString());
            }
        }
        else //if value was saved to memory
        {
            if(!FindInVM(variable)){ //if value is not already in vm
                OutputToFile.write(vm, pageToStore.toString(), true); //output to vm

            }
            else //if value is already in vm
            {
                overwriteVM(variable, pageToStore.toString()); //overwrite the value in vm with the new one
            }
        }

        //if page was still not saved and does not exist in vm, save to vm


        }

        //if there are available pages, write page at next available index
        //write page: variable ID, value, last access value = current time at command execution
        //if no available page in main memory, write page in next available disk space (check file for next empty line)
    }

    public void Release(String variable) { //remove a page with given variable
        //remove from last access key value map
        boolean foundInMem = false;

        //remove from memory if it holds a page with the same id
        for (Page MyPage : mainMemory) {
            if (MyPage.getID().equals(variable))
            {
                foundInMem = true;
                memUsed--;
                LastAccessValue.remove(MyPage.getID());
                mainMemory.remove(MyPage);
                break;
            }
        }

        if (!foundInMem && FindInVM(variable)) //if not found in memory, but found in virtual memory
        {//remove from virtual memory
            overwriteVM(variable, null);
        }
    }

    public int Lookup(String variable) { //find the value and return it of a given variable, if the page exists
        int LocatedVal = -1;

        //check main memory
        for (Page page : mainMemory) {
            if (page.getID().equals(variable)) {
                LocatedVal = page.getValue();
                LastAccessValue.put(page.getID(), Clock.INSTANCE.getTime());
                break;
            }
        }

        if(LocatedVal == -1 && FindInVM(variable)){ //if not found in memory, but found in virtual memory

            //set up swap
            int last = Clock.INSTANCE.getTime(); //set the chosen access time to highest possible value
            Page memPage = null;
            for(Page page : mainMemory){ //for each page in main memory, if it holds a lower access time, set it to the page being swapped
                int currentTime = LastAccessValue.get(page.getID());
                if( currentTime < last){
                    last = currentTime;
                    memPage = page;
                }
            }

            diskScanner.close();
            Page locatedPage = null;
            try {
                diskScanner = new Scanner(vm); //read through vm text file
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while (diskScanner.hasNextLine()) { //while there is another line of vm
                String[] line = diskScanner.nextLine().split(" ");
                if(line[0].equals(variable)) { //if the page holds the same variable being looked for
                    locatedPage = new Page(line[0], Integer.parseInt(line[1])); //set the located page to be swapped
                    LocatedVal = Integer.parseInt(line[1]); //set the located value to be returne
                }
            }

            SWAP(memPage, locatedPage); //call swap on the given pages
        }

        return LocatedVal; //return the value to be outputed
    }

    private void SWAP(Page memPage, Page vmPage) { //swap a given page from main memory with a page in virtual memory
        String SWAP = "Clock: "+Clock.INSTANCE.getTime()+", Memory Manager, SWAP: Variable "+memPage.getID()+" with Variable "+vmPage.getID();
        Clock.INSTANCE.printEvent(SWAP);
        mainMemory.remove(memPage); //remove from main memory
        LastAccessValue.remove(memPage.getID()); //remove same page from hash map
        overwriteVM(vmPage.getID(), memPage.toString()); //overwrite value taken out of memory to where the vm page is

        mainMemory.add(vmPage); //add the page taken from vm to main memory now that it has an open spot
        LastAccessValue.put(vmPage.getID(), Clock.INSTANCE.getTime()); //add a key value pair for the newly added page in main memory
    }

    public int InputReader() { //simply reads the memconfig file to get the number of pages main memory should have
        int page = 0;
        try {
            File file = new File("memconfig.txt");
            Scanner scan = new Scanner(file);
            page = scan.nextInt();
            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return page;
    }

    @Override
    public void run () { //started from the main function

        do{ //while the manager is not finished
            if(!mailbox.isEmpty()){ //if the mailbox has any messages
                interpretCommand(mailbox.remove()); //interpret the message and remove the command
            }
            else{//if the mailbox does not have any messages
                try{

                    Thread.sleep(10);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }while(!done);


    }
}