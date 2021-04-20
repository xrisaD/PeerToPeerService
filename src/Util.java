import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Util {

    public static ArrayList<String> readSharedDirectory(String path){
        ArrayList<String> allfiles = new ArrayList<String>();
        Path pa = Paths.get(path);
        File[] file = pa.toFile().listFiles();
        if (file != null) {
            for (File p : file) {
                allfiles.add(p.getName());
            }
        }
        return allfiles;
    }

    // TODO Peer ArrayList<String>
    public static ArrayList<String> readFileDownloadList(String path) {
        ArrayList<String> allfiles = new ArrayList<String>();
        File myObj = new File(path);
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String fileName = myReader.nextLine();
                allfiles.add(fileName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return allfiles;
    }
    public static byte[] loadFile (String path, String name){
        // file to bytes[]
        byte[] bytes = new byte[0];
        try {
            Path pathFile = Paths.get(path+"/"+name);
            bytes = Files.readAllBytes(pathFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void saveFile(String path, String name, byte[] bytes){
        // bytes[] to file
        Path pathFile = Paths.get(path+name);
        try {
            Files.write(pathFile, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
