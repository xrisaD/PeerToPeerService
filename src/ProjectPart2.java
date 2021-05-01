
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class ProjectPart2 {

    // Arguments:
    // 0: outPath: path that the class files are places
    // 1: tracker's input with the path of the file
    public static void main(String[] args) throws IOException {
        String outPath = args[0];
        String trackerPath = args[1];
        String ip = "127.0.0.1";
        int port = 5000;

        // Start Tracker
        String trackerCommand = String.format("java -cp %s Tracker %s %d %s" , outPath, ip, port, trackerPath);
        Process trackerProcess= Runtime.getRuntime().exec(trackerCommand);
        StreamGobbler trackerErrorGobbler = new StreamGobbler(trackerProcess.getErrorStream());
        StreamGobbler trackerOutputGobbler = new StreamGobbler(trackerProcess.getInputStream());
        trackerErrorGobbler.start();
        trackerOutputGobbler.start();

        // Start peers
        ArrayList<Process> peers = new ArrayList<>();
        String name = "name";
        String password = "password";
        // Start 10 Peers
        for (int i = 0 ; i < 10 ; i++){
            port++;
            // Start a Peer
            String peerCommand = String.format("java -cp %s Peer %s %d %s %s %s" , outPath, ip, port,name+i ,password+i, trackerPath);
            Process peerProcess = Runtime.getRuntime().exec(peerCommand);
            peers.add(peerProcess);
            StreamGobbler peerErrorGobbler = new StreamGobbler(peerProcess.getErrorStream());
            StreamGobbler peerOutputGobbler = new StreamGobbler(peerProcess.getInputStream());
            peerErrorGobbler.start();
            peerOutputGobbler.start();
        }

        Scanner sc = new Scanner(System.in);

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
