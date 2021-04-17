import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UtilTest {

    //peer 1
    String peerPath1 = "testData/peer1/shared_directory/";
    String fileName1 = "file1.txt";
    String fileNameNew = "fileNew.txt";

    //tracker
    String trackerPath = "testData/tracker/fileDownloadList.txt";


    @org.junit.Test
    public void loadFileAndSaveFile() {
        // load file and check that it isn't null
        byte[] bytes = Util.loadFile(peerPath1, fileName1);
        assertNotNull(bytes);

        //save the same file in the directory with a diferent name
        Util.saveFile(peerPath1, fileNameNew, bytes);
        // check if expected file created exists
        File f = new File(peerPath1 +"/"+fileNameNew);
        assertTrue(f.exists() && !f.isDirectory());

        //check if the content is the same
        try {
            List<String> fileOut = Files.readAllLines(Path.of(peerPath1 + "/" + fileNameNew));
            List<String> fileIn = Files.readAllLines(Path.of(peerPath1 + "/" + fileName1));
            assertEquals(fileIn, fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void readSharedDirectory() {
        ArrayList<String> array = new ArrayList<String>();
        array.add("file1.txt");
        array.add("file2.txt");
        array.add("file3.txt");
        array.add("fileNew.txt");

        ArrayList<String> allfiles = Util.readSharedDirectory(peerPath1);
        int counter = 0;
        for(String i:allfiles){
            if(allfiles.contains(i)){
                counter++;
            }
        }
        assertEquals(array.size(), allfiles.size());
        assertEquals(4, counter);
    }

    @org.junit.Test
    public void readFileDownloadList() {
        ArrayList<String> tracker = new ArrayList<String>();
        tracker.add("file1.txt");
        tracker.add("file2.txt");
        tracker.add("file3.txt");
        tracker.add("fileNew.txt");
        tracker.add("file4.txt");
        tracker.add("file5.txt");
        tracker.add("file6.txt");

        ArrayList<String> allfiles = Util.readFileDownloadList(trackerPath);
        int counter = 0;
        for(String i:allfiles){
            if(allfiles.contains(i)){
                counter++;
            }
        }
        assertEquals(tracker.size(), allfiles.size());
        assertEquals(7, counter);
    }

}