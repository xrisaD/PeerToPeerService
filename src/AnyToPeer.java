import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnyToPeer implements Serializable {
    public Method method;
    public StatusCode statusCode;

    public int token_id;
    ArrayList<String> All_files;
    ArrayList<Info> Peer_Info;

    // Bytes to sent the file
    byte[] buffer = new byte[4096];

}
