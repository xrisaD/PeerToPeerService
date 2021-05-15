import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendRequestsThread extends Thread {
    Peer p;
    String file;
    ArrayList<Info> peersWithNeededParts;
    ArrayList<Info> nonSeeders;
    ArrayList<Info> peersWithTheFile;


    SendRequestsThread(Peer p, String file, ArrayList<Info> peersWithNeededParts, ArrayList<Info> nonSeeders, ArrayList<Info> peersWithTheFile){
        this.p = p;
        this.file = file;
        this.peersWithNeededParts = peersWithNeededParts;
        this.nonSeeders = nonSeeders;
        this.peersWithTheFile = peersWithTheFile;
    }
    @Override
    public void run() {
        if(peersWithNeededParts.size()>4){
            // find 4 peers (using the rules of the requirements)

            // First Case:
            // ask 2 nonseeder for collaborativedownload
            if(nonSeeders.size()>0) {
                HashMap<Integer, ArrayList<Info>> peersForSelection = PeersUtils.getPeersCounters(p, nonSeeders);
                firstCase(peersForSelection, file, peersWithNeededParts);
            }

            // Second Case:
            // ask 2 random peers for collaborativedownload or seeder-serve
            int[] randomPeers = Util.getTwoDifferentRandomPeers(0, peersWithNeededParts.size());
            ArrayList<Info> twoRandomPeers = new ArrayList<>();
            for(int i = 0;i < randomPeers.length;i++) {
                twoRandomPeers.add(peersWithNeededParts.get(randomPeers[i]));
            }
            boolean stop = askForColDownload(twoRandomPeers, file);
            if(stop){
                System.out.println("THREAD WILL STOP");
                return;
            }

        }else{
            // ask all of them
            boolean stop = askForColDownload(peersWithTheFile, file);
            if(stop){
                System.out.println("THREAD WILL STOP");
                return;
            }
        }

    }
    private void firstCase(HashMap<Integer, ArrayList<Info>> peersForSelection, String file, ArrayList<Info> peersWithNeededChunks) {
        // get best peer or peers
        Integer max = Collections.max(peersForSelection.keySet());
        ArrayList<Info> peersWithMax = peersForSelection.get(max);

        // more than two best peers
        if (peersWithMax.size() > 2) {
            // select 2 random peers
            int[] randomPeers = Util.getTwoDifferentRandomPeers(0, peersWithMax.size());
            ArrayList<Info> twoRandomPeers = new ArrayList<>();
            for(int i = 0;i < randomPeers.length;i++) {
                twoRandomPeers.add(peersWithNeededChunks.get(randomPeers[i]));
            }
            boolean stop = askForColDownload(twoRandomPeers, file);
            if(stop){
                System.out.println("THREAD WILL STOP");
                return;
            }

        } else if (peersWithMax.size() == 2) {
            boolean stop = askForColDownload(peersWithMax, file);
            if(stop){
                System.out.println("THREAD WILL STOP");
                return;
            }
        } else {
            // send to the best peer
            boolean stop = askForColDownload(peersWithMax.get(0), file);
            if(stop){
                System.out.println("THREAD WILL STOP");
                return;
            }
            peersWithMax.remove(max);
            if (peersWithMax.size()>0) {
                Integer max2 = Collections.max(peersForSelection.keySet()); // second
                ArrayList<Info> peersWithMax2 = peersForSelection.get(max2);
                if (peersWithMax2.size() > 1) {
                    // select 1 random peers
                    int random = ThreadLocalRandom.current().nextInt(0, peersWithMax2.size());
                    stop = askForColDownload(peersWithMax2.get(random), file);
                    if(stop){
                        System.out.println("THREAD WILL STOP");
                        return;
                    }
                } else {
                    stop = askForColDownload(peersWithMax2.get(0), file);
                    if(stop){
                        System.out.println("THREAD WILL STOP");
                        return;
                    }
                }
            }
        }

    }


    public boolean askForColDownload( ArrayList<Info> peersWithTheFile, String file){
        for (int i=0; i<peersWithTheFile.size(); i++){
            boolean stop = askForColDownload(peersWithTheFile.get(i), file);
            if(stop){
                return stop;
            }
        }
        return false;
    }


    public boolean askForColDownload(Info peersWithTheFile, String file){
        Method method;
        if(peersWithTheFile.seederBit.get(file)){
            method = Method.SEEDER_SERVE;
        }else{
            method = Method.COLLABORATIVE_DOWNLOAD;
        }
        p.collaborativeDownloadOrSeeederServe(method, file, peersWithTheFile, p.myInfo, null, -1);

        // check each time if the thread has been interrupted
        // this will happen when 2 secs passed
        return Thread.currentThread().isInterrupted();
    }
}
