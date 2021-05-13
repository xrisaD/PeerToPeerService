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
        //TODO: updatefileToNumberOfPartions

        ArrayList<String> peersFiles = p.fileNames; // peer's files
        //TODO: nonCompletedParts: gemisma me ta onomata olwn twn arxeiwn tou susthmatos

        ArrayList<String> forDownload = Util.difference(allFiles, peersFiles); // the files that the peer doesn't have

        String file = Util.select(forDownload); // select the next file for download

        while(forDownload.size()>0){

            ArrayList<Info> peersWithTheFile = p.details(file); // get file's details
            ArrayList<>
            //TODO: updatefileToNumberOfPartions

            // find the number of file's chunks
            // initialize a tmp array with the the chunks we have
            //TODO: check oti den einai seeder k an einai katallhlo...
            ArrayList<Info> peersWithNeededChunks = ; // peer with chunks that we don't have
            getNonSeeders();
            getSeeders();
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

    public ArrayList<Info> getSeeders( ArrayList<Info> peersWithTheFile, String filename){
        ArrayList<Info> infoOnlyForSeeders = new ArrayList<>();
        for(int i=0; i<peersWithTheFile.size(); i++){
            if(peersWithTheFile.get(i).seederBit.get(filename)){
                infoOnlyForSeeders.add(peersWithTheFile.get(i));
            }
        }
        return infoOnlyForSeeders;
    }

    public ArrayList<Info> getNonSeeders( ArrayList<Info> peersWithTheFile, String filename, ArrayList<Partition> myParts){
        ArrayList<Info> infoOnlyForNonSeeders = new ArrayList<>();
        ArrayList<Integer> partitions = new ArrayList<>();
        for(int i=0; i<myParts.size(); i++){
            partitions.add(myParts.get(i).id);
        }

        for(int i=0; i<peersWithTheFile.size(); i++){
            if((!peersWithTheFile.get(i).seederBit.get(filename)) && Util.differencePieces(peersWithTheFile.get(i).pieces.get(filename), partitions).size()>0);{
                infoOnlyForNonSeeders.add(peersWithTheFile.get(i));
            }
        }
        return infoOnlyForNonSeeders;
    }
}
