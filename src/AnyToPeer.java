import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class AnyToPeer implements Serializable {
    public Method method;
    public StatusCode statusCode;
    byte[] buffer = new byte[4096];
    public int token_id;
    ArrayList<String> All_files;
    ArrayList<Info> Peer_Info;
    // Details reply from tracker

    @Override
    public String toString() {
        return "AnyToPeer{" +
                "method=" + method +
                ", statusCode=" + statusCode +
                ", token_id=" + token_id +
                ", All_files=" + All_files +
                ", Peer_Info=" + Peer_Info +
                '}';
    }
}
