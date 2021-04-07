import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Util {

    public static ArrayList<String> readfiledownloadlist(String path){
        ArrayList<String> allfiles = new ArrayList<String>();
        Path pa = Paths.get(path);
        File[] file = pa.toFile().listFiles();
        if (file != null) {
            for (File p : file) {
                allfiles.add(p.getName());
            }
        }
        return allfiles;

//        ArrayList<String> array = new ArrayList<String>();
//        array.add("file1.txt");
//        array.add("file2.txt");
//        array.add("file3.txt");
//        return array;
    }

    // TODO Peer ArrayList<String>

    public static byte[] loadFile (String path, String name){
        System.out.printf("Loading file: %s",name);
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
