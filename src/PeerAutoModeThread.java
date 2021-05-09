import java.util.ArrayList;

public class PeerAutoModeThread extends Thread {
    Peer p;
    PeerAutoModeThread(Peer p){
        this.p = p;
    }
    @Override
    public void run() {
        System.out.println("Start peer..");
        StatusCode statusCode = this.p.register();
        System.out.println("[PEER] REGISTER " + statusCode + " " + this.p.getIp() + " " + this.p.getPort());

        statusCode = this.p.login();
        System.out.println("[PEER] LOGIN " + statusCode + " " + this.p.getIp() + " " + this.p.getPort());

        ArrayList<String> allFiles = p.list(); // all system's files
        ArrayList<String> peersFiles = p.fileNames; // peer's files
        ArrayList<String> forDownload = Util.difference(allFiles, peersFiles); // the files that the peer doesn't have

        while(forDownload.size()>0){
            String file = Util.select(forDownload); // select the next file for download

            // details

            // while not all -> download

            // ALL LOGIC GOES HERE
        }


    }
}
