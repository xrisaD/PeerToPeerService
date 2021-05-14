import java.util.ArrayList;
import java.util.HashMap;

public class SendRequestsThread extends Thread {
    Peer p;
    SendRequestsThread(Peer p){
        this.p = p;
    }
    @Override
    public void run() {
        if(peersWithNeededParts.size()>0){
            if(peersWithNeededParts.size()>4){
                // find 4 peers (using the rules of the requirements)

                // First Case:
                // ask 2 nonseeder for collaborativedownload
                if(nonSeeders.size()>0) {
                    HashMap<Integer, ArrayList<Info>> peersForSelection = getPeersCounters(nonSeeders);
                    firstCase(peersForSelection, file, peersWithNeededParts);
                }

                // Second Case:
                // ask 2 random peers for collaborativedownload or seeder-serve
                int[] randomPeers = Util.getTwoDifferentRandomPeers(0, peersWithNeededParts.size());
                ArrayList<Info> twoRandomPeers = new ArrayList<>();
                for(int i = 0;i < randomPeers.length;i++) {
                    twoRandomPeers.add(peersWithNeededParts.get(randomPeers[i]));
                }
                askForColDownload(twoRandomPeers, file);

            }else{
                // ask all of them
                askForColDownload(peersWithTheFile, file);
            }
        }else{
            // noone has the file
            continue;
        }
    }
}
