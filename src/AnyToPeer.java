import java.io.Serializable;
import java.util.ArrayList;

public class AnyToPeer implements Serializable {
    public Method method;
    public StatusCode statusCode;
    byte[] buffer = new byte[4096];
    public int token_id;
    ArrayList<String> All_files;
    ArrayList<Info> Peer_Info;


}
