import java.io.Serializable;
import java.util.ArrayList;

// AnytoPeer: messages between peers and tracker to peer
public class AnyToPeer implements Serializable {
    public Method method;
    public StatusCode statusCode;
    byte[] buffer = new byte[4096];
    public int tokenΙd;
    ArrayList<String> allFiles;
    ArrayList<Info> peerInfo;
    String fileName;
    // Details reply from tracker

    @Override
    public String toString() {
        return "AnyToPeer{" +
                "method=" + method +
                ", statusCode=" + statusCode +
                ", token_id=" + tokenΙd +
                ", All_files=" + allFiles +
                ", Peer_Info=" + peerInfo +
                '}';
    }
}
