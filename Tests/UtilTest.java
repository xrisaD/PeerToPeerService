import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class UtilTest {

    @org.junit.Test
    public void loadFileAndSaveFile() {
        String path = "C:/Users/Xrisa/IdeaProjects/Sockets-ComputerNetworks/testData/peer1/shared_directory/";
        String fileName1 = "file1.txt";
        String fileName2 = "fileNew.txt";

        // load file and check that it isn't null
        byte[] bytes = Util.loadFile(path, fileName1);
        assertNotNull(bytes);

        //save the same file in the directory with a diferent name
        Util.saveFile(path, fileName2, bytes);
        // check if expected file created exists
        File f = new File(path+"/"+fileName2);
        assertTrue(f.exists() && !f.isDirectory());

        //check if the content is the same
        try {
            List<String> fileOut = Files.readAllLines(Path.of(path + "/" + fileName2));
            List<String> fileIn = Files.readAllLines(Path.of(path + "/" + fileName1));
            assertEquals(fileIn, fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}