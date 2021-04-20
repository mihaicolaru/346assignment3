import java.io.FileWriter;
import java.io.*;

public class OutputToFile {
    public static void write(File file,String outputString, boolean append){
        try{
            FileWriter writer = new FileWriter(file, append); //creates new instance of a filewriter with passed values
            writer.write(outputString + System.lineSeparator());  //output given string to file and bring cursor to next line
            writer.close(); //free up writer
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
