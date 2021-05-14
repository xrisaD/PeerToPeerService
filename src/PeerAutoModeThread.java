import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

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
        //TODO: updatefileToNumberOfPartions

        ArrayList<String> peersFiles = p.fileNames; // peer's files



        //TODO: nonCompletedParts: gemisma me ta onomata olwn twn arxeiwn tou susthmatos

        ArrayList<String> forDownload = Util.difference(allFiles, peersFiles); // the files that the peer doesn't have

        String file = Util.select(forDownload); // select the next file for download

        while(forDownload.size()>0){

            ArrayList<Info> peersWithTheFile = p.details(file); // get file's details


            ArrayList<Info> peersWithNeededParts = getPeersWithNeededChunks(peersWithTheFile, file, p.nonCompletedFiles.get(file)); // peer with chunks that we don't have
            ArrayList<Info> seeders = getSeeders(peersWithNeededParts, file); // file's seeders
            ArrayList<Info> nonSeeders = getNonSeeders(peersWithNeededParts, file);

            // get the number of parts that a file has
            // a seeder knows all the pieces
            int numOfParts = 0;
            if(seeders.size()>0) {
                numOfParts = seeders.get(0).pieces.get(file).size();
                p.fileToNumberOfPartitions.put(file, numOfParts);
            }
            // if peer didn't know about the file
            // it is the first time that he will ask for a piece
            if (!p.nonCompletedFiles.containsKey(file)){
                p.nonCompletedFiles.put(file, new ArrayList<>());
            }

            // if peer has all the parts
            if ( numOfParts ==  p.nonCompletedFiles.get(file).size()){
                // actions of becoming a seeder of a file:
                // 1st: update your structures
                // 2nd: inform tracker that now you are a seeder because you have all the parts
                // 3rd: don't send any requests of parts of the file

                // 1st:
                ArrayList<Partition> parts = p.nonCompletedFiles.get(file);
                p.nonCompletedFiles.remove(file);
                // assemble file and save it to the share directory
                byte[][] partsData = Util.findOrder(parts);
                Util.saveFile(p.sharedDirectoryPath, file, Util.assemble(partsData));
                p.completedFiles.put(file, partsData);
                // 2nd:
                p.iAmASeeder(file);
                // 3rd:
                continue;
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
            askForColDownload(twoRandomPeers, file);

        } else if (peersWithMax.size() == 2) {
            askForColDownload(peersWithMax, file);
        } else {
            // send to the best peer
            askForColDownload(peersWithMax.get(0), file);
            peersWithMax.remove(max);
            if (peersWithMax.size()>0) {
                Integer max2 = Collections.max(peersForSelection.keySet()); // second
                ArrayList<Info> peersWithMax2 = peersForSelection.get(max2);
                if (peersWithMax2.size() > 1) {
                    // select 1 random peers
                    int random = ThreadLocalRandom.current().nextInt(0, peersWithMax2.size());
                    askForColDownload(peersWithMax2.get(random), file);
                } else {
                    askForColDownload(peersWithMax2.get(0), file);
                }
            }
        }

    }

    // Counter -> peers with this counter
    private HashMap<Integer, ArrayList<Info>> getPeersCounters(ArrayList<Info> nonSeeders) {
        HashMap<Integer, ArrayList<Info>> peersForSelection = new HashMap<>();
        // get the counters
        for (int i = 0; i < nonSeeders.size(); i++) {
            if (p.usernameToDownloadedFiles.contains(nonSeeders.get(i).username)) {
                int counter = p.usernameToDownloadedFiles.get(nonSeeders.get(i).username);
                if (!peersForSelection.containsKey(counter)) {
                    peersForSelection.put(counter, new ArrayList<>());
                }
                peersForSelection.get(counter).add(nonSeeders.get(i));
            } else {
                if (!peersForSelection.containsKey(0)) {
                    peersForSelection.put(0, new ArrayList<>());
                }
                // we haven't received from this peer yet
                peersForSelection.get(0).add(nonSeeders.get(i));
            }
        }
        return peersForSelection;
    }

    public void askForColDownload( ArrayList<Info> peersWithTheFile, String file){
        for (int i=0; i<peersWithTheFile.size(); i++){
            askForColDownload(peersWithTheFile.get(i), file);
        }
    }

    public void askForColDownload(Info peersWithTheFile, String file){
        Method method;
        if(peersWithTheFile.seederBit.get(file)){
            method = Method.SEEDER_SERVE;
        }else{
            method = Method.COLLABORATIVE_DOWNLOAD;
        }
        p.collaborativeDownloadOrSeeederServe(method, file, peersWithTheFile, p.myInfo, null, -1);

    }
    public ArrayList<Info> getSeeders( ArrayList<Info> peersWithTheFile, String filename){
        ArrayList<Info> infoOnlyForSeeders = new ArrayList<>();
        for(int i=0; i<peersWithTheFile.size(); i++){
            if(peersWithTheFile.get(i).seederBit.get(filename)){
                infoOnlyForSeeders.add(peersWithTheFile.get(i));
            }
        }
        return infoOnlyForSeeders;
    }

    public ArrayList<Info> getNonSeeders( ArrayList<Info> peersWithTheFile, String filename){
        ArrayList<Info> infoOnlyForSeeders = new ArrayList<>();
        for(int i=0; i<peersWithTheFile.size(); i++){
            if(!peersWithTheFile.get(i).seederBit.get(filename)){
                infoOnlyForSeeders.add(peersWithTheFile.get(i));
            }
        }
        return infoOnlyForSeeders;
    }

    public ArrayList<Info> getPeersWithNeededChunks( ArrayList<Info> peersWithTheFile, String filename, ArrayList<Partition> myParts){
        ArrayList<Info> infoOnlyForNonSeeders = new ArrayList<>();
        ArrayList<Integer> partitions = new ArrayList<>();
        for(int i=0; i<myParts.size(); i++){
            partitions.add(myParts.get(i).id);
        }

        for(int i=0; i<peersWithTheFile.size(); i++){
            if( Util.differencePieces(peersWithTheFile.get(i).pieces.get(filename), partitions).size()>0);{
                infoOnlyForNonSeeders.add(peersWithTheFile.get(i));
            }
        }
        return infoOnlyForNonSeeders;
    }
}
