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

        String file = Util.select(forDownload); // select the next file for download

        while(forDownload.size()>0){

            ArrayList<Info> peersWithTheFile = p.details(file); // get file's details

            // find the number of file's chunks
            // initialize a tmp array with the the chunks we have

            ArrayList<Info> peersWithNeededChunks; // peer with chunks that we don't have

            if(peersWithNeededChunks.size()>0){
                // while not all -> download
                if(peersWithNeededChunks.size()>4){
                    // find 4 peers (using the rules of the requirements)

                }else{
                    // ask all of them
                }
                // ALL LOGIC GOES HERE
            }else{
                continue;
            }

        }


    }
}
