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

        while(forDownload.size()>0){
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String file = Util.select(forDownload); // select the next file for download
            System.out.println("FILE_NAME: " + file);
            ArrayList<Info> peersWithTheFile = p.details(file); // get file's details
            if(peersWithTheFile!=null && peersWithTheFile.size()>0){
                System.out.println("PEERS_WITH THE FILE: " + peersWithTheFile.size());
                if(!p.nonCompletedFiles.containsKey(file)){
                    p.nonCompletedFiles.put(file, new ArrayList<>());
                }

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
                    System.out.println("I AM PEER WITH PORT " + p.getPort() + " AND I AM A SEEDER BECAUSE I GOT " + numOfParts + " NUMBER OF PARTS FOR FILE: " + file);
                    // actions of becoming a seeder of a file:
                    // 1st: update your structures
                    // 2nd: inform tracker that now you are a seeder because you have all the parts
                    // 3rd: delete tmp files
                    // 4th: don't send any requests of parts of the file

                    // 1st:
                    ArrayList<Partition> parts = p.nonCompletedFiles.get(file);
                    p.nonCompletedFiles.remove(file);
                    forDownload.remove(file);
                    // assemble file and save it to the share directory
                    byte[][] partsData = Util.findOrder(parts);
                    Util.saveFile(p.sharedDirectoryPath, file, Util.assemble(partsData));
                    p.completedFiles.put(file, partsData);
                    // 2nd:
                    p.iAmASeeder(file);
                    // 3rd:
                    ArrayList<String> allTmps = Util.readDirectory(p.tmpPath);
                    Util.deleteFiles(allTmps, file, p.tmpPath);
                    // 4th
                    continue;
                }

                // at least one peer has the file we need
                if(peersWithNeededParts.size()>0){
                    SendRequestsThread sendRequestsThread = new SendRequestsThread(this.p ,file, peersWithNeededParts, nonSeeders, peersWithTheFile);
                    sendRequestsThread.start();

                    Sleep thread200 = new Sleep(p);
                    thread200.start();

                    while(true){
                        try {
                            sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (p.state){
                            if(p.state.isAtLeastOneDone()){
                                if(p.state.done4){
                                    System.out.println("I AM PEER WITH PORT " + p.getPort() + " AND WILL CONTINUE BECAUSE I GOT 4 REQUESTS!");
                                }else{
                                    System.out.println("I AM PEER WITH PORT " + p.getPort() + " AND WILL CONTINUE BECAUSE 200 PASSED!");
                                }
                                sendRequestsThread.interrupt();
                                break;
                            }
                        }
                    }
                }
            }else{
                // no one has the file
                continue;
            }
        }
        System.out.println("I AM PEER WITH PORT " + p.getPort() + " AND I DOWNLOADED ALL THE FILES!");

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
