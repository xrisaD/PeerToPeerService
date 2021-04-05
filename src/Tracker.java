import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers;
    ConcurrentHashMap<Integer, Info> TokenId_toInfo;
    ConcurrentHashMap<String, ArrayList<Integer>> Files_toToken;
    ArrayList<Integer> All_tokenids;
    ArrayList<String> All_files;
    private String ip;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    private int port;

    public Tracker(String ip, int port) {
        this.ip = ip;
        this.port = port;
        //TODO: Read TXT and fill the All_files array;
    }

    public void startServer() {
        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(this.port, 10);
            System.out.println("Tracker listening on port " + getPort());
            while (true) {
                connection = providerSocket.accept();
                //We start a thread
                //this thread will do the communication
                ΤrackerHandler ph = new ΤrackerHandler(connection);
                ph.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        try{
            Tracker p = new Tracker(args[0],Integer.parseInt(args[1]));
            p.startServer();

        }catch (Exception e) {
            System.out.println("Usage: java Publisher ip port");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public class ΤrackerHandler extends Thread{
        Socket socket;
        public ΤrackerHandler(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run(){ //Protocol
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try{
                in = new ObjectInputStream(socket.getInputStream());
                PeerToTracker req= (PeerToTracker) in.readObject();
                System.out.printf("[Tracker %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
                out = new ObjectOutputStream(socket.getOutputStream());

                if(req.method == Method.REGISTER){
                    notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
                }else if(req.method == Method.LOGIN){
                    push(req.artistName, req.songName.toLowerCase(), out);
                }else if(req.method == Method.LOGOUT){

                }
                //if(Registered_peers.contains())

//                Request.RequestToPublisher req= (Request.RequestToPublisher) in.readObject();
//                System.out.printf("[PUBLISHER %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
//                out = new ObjectOutputStream(socket.getOutputStream());
//                if(req.method == Request.Methods.PUSH) {
//                    if(req.artistName==null || req.songName==null){
//                        notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                    }else {
//                        push(req.artistName, req.songName.toLowerCase(), out);
//                    }
//                }else if(req.method == Request.Methods.SEARCH){
//                    if(req.artistName==null){
//                        notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                    }else{
//                        search(req.artistName, out);
//                    }
//                }else{
//                    notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if(socket != null) socket.close();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
}
