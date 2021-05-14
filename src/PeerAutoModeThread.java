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


            ArrayList<Info> peersWithNeededChunks = getPeersWithNeededChunks(peersWithTheFile, file, p.nonCompletedFiles.get(file)); // peer with chunks that we don't have
            ArrayList<Info> seeders = getSeeders(peersWithNeededChunks, file); // file's seeders
            ArrayList<Info> nonSeeders = getNonSeeders(peersWithNeededChunks, file);

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

            if(peersWithNeededChunks.size()>0){
                if(peersWithNeededChunks.size()>4){
                    // find 4 peers (using the rules of the requirements)
                    // ask 2 nonseeder for collaborativedownload
                    int[] randomSeeders = Util.getTwoDifferentRandomFiles(0, nonSeeders.size());
                    for(int i = 0;i < randomSeeders.length;i++) {
                        p.collaborativeDownloadOrSeeederServe(Method.COLLABORATIVE_DOWNLOAD, file, nonSeeders.get(i), p.myInfo, null, -1);
                    }


                    // ask 2 random peers for collaborativedownload or seeder-serve
                    int[] randomPeers = Util.getTwoDifferentRandomFiles(0, peersWithNeededChunks.size());
                    ArrayList<Info> twoRandomPeers = new ArrayList<>();
                    for(int i = 0;i < randomPeers.length;i++) {
                        twoRandomPeers.add(peersWithNeededChunks.get(randomPeers[i]));
                    }
                    askForColDownload(twoRandomPeers, file);

                }else{
                    // ask all of them
                    askForColDownload(peersWithTheFile, file);
                }
                // ALL LOGIC GOES HERE
            }else{
                continue;
            }

        }

    }
    public void askForColDownload( ArrayList<Info> peersWithTheFile, String file){
        for (int i=0; i<peersWithTheFile.size(); i++){
            Method method;
            if(peersWithTheFile.get(i).seederBit.get(file)){
                method = Method.SEEDER_SERVE;
            }else{
                method = Method.COLLABORATIVE_DOWNLOAD;
            }
            p.collaborativeDownloadOrSeeederServe(method, file, peersWithTheFile.get(i), p.myInfo, null, -1);
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
