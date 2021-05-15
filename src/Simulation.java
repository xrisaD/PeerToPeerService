
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Simulation {

    // Arguments:
    // 0: outPath: path that the class files are places
    // 1: tracker's input with the path of the file
    // 2: testData path
    public static void main(String[] args) throws IOException {
        int PEERS = 2;
        String outPath = args[0];
        String trackerPath = args[1];
        String testDataPath = args[2];

        // Start Tracker
        String trackerCommand = String.format("java -cp %s Tracker %s" , outPath, trackerPath);
        Process trackerProcess= Runtime.getRuntime().exec(trackerCommand);
        StreamGobbler trackerErrorGobbler = new StreamGobbler(trackerProcess.getErrorStream());
        StreamGobbler trackerOutputGobbler = new StreamGobbler(trackerProcess.getInputStream());
        trackerErrorGobbler.start();
        trackerOutputGobbler.start();

        Scanner sc = new Scanner(System.in);
        System.out.println("Press 0 to continue...");
        while(true) {
            String line = sc.nextLine();
            if (line.trim().toLowerCase().equals("0")) {
                System.out.println("Continue with peers...");
                break;
            }
        }

        // we will use testData path in order to create the path for each on the peers
        // format: PATH + peerX\shared_directory\
        ArrayList<String> sharedDirectories = new ArrayList<String>();
        for (int i = 0 ; i < PEERS ; i++){
            String path = testDataPath + "peer" + i + "/shared_directory/";
            sharedDirectories.add(path);
        }

        String ip = "127.0.0.1";
        int port = 5000;
        // Start peers
        ArrayList<Process> peers = new ArrayList<>();
        String name = "name";
        String password = "password";
        // Start 10 Peers
        for (int i = 0 ; i < PEERS ; i++){
            port++;
            // Start a Peer
            String peerCommand = String.format("java -cp %s Peer %s %d %s %s %s %b" , outPath, ip, port,name+i ,password+i, sharedDirectories.get(i), true);
            Process peerProcess = Runtime.getRuntime().exec(peerCommand);
            peers.add(peerProcess);
            StreamGobbler peerErrorGobbler = new StreamGobbler(peerProcess.getErrorStream());
            StreamGobbler peerOutputGobbler = new StreamGobbler(peerProcess.getInputStream());
            peerErrorGobbler.start();
            peerOutputGobbler.start();
        }

        sc = new Scanner(System.in);

        while(true) {
            String line = sc.nextLine();
            if (line.trim().toLowerCase().equals("0")) {
                System.out.println("Exit");
                break;
            }
        }
        System.out.println("Destroying processes..");
        trackerProcess.destroy();
        for(Process p : peers){
            p.destroy();
        }
    }
    static class StreamGobbler extends Thread {
        InputStream is;
        // reads everything from is until empty.
        StreamGobbler(InputStream is) {
            this.is = is;
        }
        public void run() {
            Scanner sc = new Scanner(is);
            String line = null;
            try {
                while ((line = sc.nextLine()) != null) {
                    System.out.println(line);
                }
            }
            //Sometimes no such element exception occurs when a process is destroyed
            catch(Exception e){
                System.out.println("Stream gobbler " + this + " terminating");
            }
        }
    }
}
