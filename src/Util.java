import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    // read all files' names in a specific directory
    public static ArrayList<String> readDirectory(String path){
        ArrayList<String> allfiles = new ArrayList<String>();
        Path pa = Paths.get(path);
        File[] file = pa.toFile().listFiles();
        if (file != null) {
            for (File p : file) {
                if (p.isFile()) {
                    allfiles.add(p.getName());
                }
            }
        }
        return allfiles;
    }

    // read the list with all files
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

    // load a file
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

    // save a given array of bytes in a specific path
    public static void saveFile(String path, String name, byte[] bytes){
        // bytes[] to file
        Path pathFile = Paths.get(path+'/'+name);
        try {
            Files.write(pathFile, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // divide an array of bytes
    public static byte[][] divide(byte[] in, int partitionSize) {
        int partitionCount =  (int)Math.ceil((double)in.length / (double) partitionSize);

        byte[][] temp = new byte[partitionCount][];

        for (int p = 0; p < partitionCount; p++)
        {
            int start = p * partitionSize;
            int len = (p != partitionCount - 1) ? partitionSize : in.length - start;
            byte[] partition = new byte[len];

            System.arraycopy(in, start, partition, 0, len);

            temp[p] = partition;
        }

        return temp;
    }

    public static byte[] assemble(byte[][] in){
        int size = 0;
        if(in.length==1) {
            size = in[0].length;
        }else{
            size = (in.length - 1)*in[0].length + in[in.length-1].length;
        }
        byte[] out = new byte[size];
        int s = 0;

        for (int p = 0; p < in.length; p++){
            for (int i = 0; i < in[p].length; i++){
                out[s] = in[p][i];
                s++;
            }
        }

        return out;
    }

    public static ArrayList<String> difference(ArrayList<String> all, ArrayList<String> sp){
        ArrayList<String> differences = new ArrayList<>(all);
        differences.removeAll(sp);
        return differences;
    }

    public static ArrayList<Integer> differencePieces(ArrayList<Integer> all, ArrayList<Integer> sp){
        ArrayList<Integer> differences = new ArrayList<>(all);
        differences.removeAll(sp);
        return differences;
    }

    // select randomly a file
    public static String select(ArrayList<String> files){
        if(files.size()>0) {
            int x =  ThreadLocalRandom.current().nextInt(0, files.size());
            return files.get(x);
        }else{
            return null;
        }
    }

    public static ArrayList<Integer> getNumbersInRange(int start, int end) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(i);
        }
        return result;
    }

    public static int[] getTwoDifferentRandomPeers(int start, int end){
        int[] randomnums = new int[2];
        int random1 = ThreadLocalRandom.current().nextInt(start, end);
        randomnums[0] = random1;
        int random2;
        do {
            random2 = ThreadLocalRandom.current().nextInt(start, end);
        }while (random1!=random2);
        randomnums[1] = random2;
        return randomnums;
    }

    public static byte[][] findOrder(ArrayList<Partition> parts){
        byte[][] result = new byte[parts.size()][];
        Collections.sort(parts);
        for (int i=0; i<parts.size(); i++){
            result[i] = parts.get(i).data;
        }
        return result;
    }

    // delete a file
    public static void deleteFile(String filePath, String fileName){
        File myObj = new File(filePath + "/" + fileName);
        System.out.println(myObj.getAbsolutePath());
        myObj.delete();
    }

    // delete all files which starts from a specific string
    // these files are part of a file
    public static void deleteFiles(ArrayList<String> files, String file, String filePath){
        String fileStart = file.substring(0, file.indexOf(".txt"));
        for (int i = 0; i < files.size(); i++) {
            String fileName = files.get(i);
            System.out.println(fileName);
            if(fileName.startsWith(fileStart)) {
                deleteFile(filePath, fileName);
            }
        }
    }



}
