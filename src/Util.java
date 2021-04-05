import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Util {
    public static ArrayList<File> Readfiledownloadlist(Path path){
        ArrayList<File> allfiles = new ArrayList<File>();
        File[] file = path.toFile().listFiles();
        if (file != null) {
            for (File p : file) {
                allfiles.add(p);
            }
        }
        return allfiles;
    }

    public static byte[] Loadfile {

    }

}
